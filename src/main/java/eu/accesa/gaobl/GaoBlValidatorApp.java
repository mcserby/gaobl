package eu.accesa.gaobl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import eu.accesa.gaobl.dto.Wallet;
import eu.accesa.gaobl.dto.CoinTransactionRequest;
import eu.accesa.gaobl.service.CoinTransactionService;
import eu.accesa.gaobl.service.WalletService;
import io.grpc.stub.StreamObserver;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tendermint.abci.ABCIApplicationGrpc;
import tendermint.abci.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static eu.accesa.gaobl.SecurityUtils.OM;
import static eu.accesa.gaobl.constants.Operations.CREATE_WALLET;
import static eu.accesa.gaobl.constants.Operations.SEND_COINS;

public class GaoBlValidatorApp extends ABCIApplicationGrpc.ABCIApplicationImplBase {

    Logger logger = LoggerFactory.getLogger(GaoBlValidatorApp.class);


    private Environment env;
    private Transaction txn = null;
    private Store store = null;

    private WalletService walletService = new WalletService();
    private CoinTransactionService coinTransactionService = new CoinTransactionService();

    GaoBlValidatorApp(Environment env) {
        this.env = env;
    }

    @Override
    public void echo(Types.RequestEcho request, StreamObserver<Types.ResponseEcho> responseObserver) {
        var response = Types.ResponseEcho.newBuilder().setMessage(request.getMessage()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void info(Types.RequestInfo request, StreamObserver<Types.ResponseInfo> responseObserver) {
        String version = request.getVersion();
        var response = Types.ResponseInfo.newBuilder().setVersion(version).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void initChain(Types.RequestInitChain request, StreamObserver<Types.ResponseInitChain> responseObserver) {
        Types.ConsensusParams consensusParams = request.getConsensusParams();
        List<Types.ValidatorUpdate> validatorsList = request.getValidatorsList();

        String publicKey = "MIIBtzCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYQAAoGAestXy+psFPjJ/qtXIcFyAyZjGuMlMtd8UdGUg8Ma+f196PvOvUE390oQbg9Q2oyywUDDzHdXSJaCxBjtnqAODtdQveF+3+dSe2I7CuwARwr/4jPJJo1wcNFVmkHZhhfvsQMVPKkdJHp60iKhgoLBB4F3Q56fQ5Xn5GaqT7b5oJI=";
        Wallet wallet = new Wallet(UUID.randomUUID().toString(), 100000000, publicKey, "cto");
        WalletRepository walletRepository = new WalletRepository();
        walletRepository.saveWallet(wallet);

        var response = Types.ResponseInitChain.newBuilder()
                .addAllValidators(validatorsList)
                .setConsensusParams(consensusParams)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void beginBlock(Types.RequestBeginBlock req, StreamObserver<Types.ResponseBeginBlock> responseObserver) {
        txn = env.beginTransaction();
        store = env.openStore("store", StoreConfig.WITHOUT_DUPLICATES, txn);
        var resp = Types.ResponseBeginBlock.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deliverTx(Types.RequestDeliverTx req, StreamObserver<Types.ResponseDeliverTx> responseObserver) {
        var tx = req.getTx();
        int code = validate(tx);

        List<byte[]> parts = split(tx, '=');

        String value = new String(new ArrayByteIterable(parts.get(1)).getBytesUnsafe());
        String key = new String(new ArrayByteIterable(parts.get(0)).getBytesUnsafe());

        switch (key) {
            case CREATE_WALLET:
                walletService.processWallet(value);
                break;
            case SEND_COINS:
                coinTransactionService.processCoinTransaction(value);
                break;
            default:
                break;
        }

        //TODO: set as response wallet or transaction result.
        Types.EventAttribute address = Types.EventAttribute.newBuilder()
                .setKey(ByteString.copyFromUtf8("walletId"))
                .setValue(ByteString.copyFromUtf8("Cluj"))
                .setIndex(true)
                .build();
        Types.EventAttribute amount = Types.EventAttribute.newBuilder()
                .setKey(ByteString.copyFromUtf8("amount"))
                .setValue(ByteString.copyFromUtf8("0"))
                .setIndex(true)
                .build();

        List<Types.EventAttribute> attributesList = new ArrayList<>();
        attributesList.add(address);
        attributesList.add(amount);

        Types.Event event = Types.Event.newBuilder()
                .setType("validator.provisions")
                .addAllAttributes(attributesList)
                .build();

        var resp = Types.ResponseDeliverTx.newBuilder()
                .setCode(code)
                .addEvents(event)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void commit(Types.RequestCommit req, StreamObserver<Types.ResponseCommit> responseObserver) {
        txn.commit();
        var resp = Types.ResponseCommit.newBuilder()
                .setData(ByteString.copyFrom(new byte[8]))
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void query(Types.RequestQuery req, StreamObserver<Types.ResponseQuery> responseObserver) {
        var k = req.getData().toByteArray();
        var v = getPersistedValue(k);
        var builder = Types.ResponseQuery.newBuilder();
        if (v == null) {
            builder.setLog("does not exist");
        } else {
            builder.setLog("exists");
            builder.setKey(ByteString.copyFrom(k));
            builder.setValue(ByteString.copyFrom(v));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void checkTx(Types.RequestCheckTx req, StreamObserver<Types.ResponseCheckTx> responseObserver) {
        var tx = req.getTx();
        int code = validate(tx);
        var resp = Types.ResponseCheckTx.newBuilder()
                .setCode(code)
                .setGasWanted(1)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void flush(Types.RequestFlush request, StreamObserver<Types.ResponseFlush> responseObserver) {
        var response = Types.ResponseFlush.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void setOption(Types.RequestSetOption request, StreamObserver<Types.ResponseSetOption> responseObserver) {
        var response = Types.ResponseSetOption.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void endBlock(Types.RequestEndBlock request, StreamObserver<Types.ResponseEndBlock> responseObserver) {
        var response = Types.ResponseEndBlock.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listSnapshots(Types.RequestListSnapshots request, StreamObserver<Types.ResponseListSnapshots> responseObserver) {
        var response = Types.ResponseListSnapshots.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void offerSnapshot(Types.RequestOfferSnapshot request, StreamObserver<Types.ResponseOfferSnapshot> responseObserver) {
        var response = Types.ResponseOfferSnapshot.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void loadSnapshotChunk(Types.RequestLoadSnapshotChunk request, StreamObserver<Types.ResponseLoadSnapshotChunk> responseObserver) {
        var response = Types.ResponseLoadSnapshotChunk.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void applySnapshotChunk(Types.RequestApplySnapshotChunk request, StreamObserver<Types.ResponseApplySnapshotChunk> responseObserver) {
        var response = Types.ResponseApplySnapshotChunk.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private int validate(ByteString tx) {
        try {
            List<Wallet> wallets = walletService.findWallets();

            List<byte[]> parts = split(tx, '=');
            logger.info("transaction message: " + parts.get(0).toString() + " : " + parts.get(1).toString());
            if (parts.size() != 2) {
                return 1;
            }

            String key = new String(new ArrayByteIterable(parts.get(0)).getBytesUnsafe());
            String value = new String(new ArrayByteIterable(parts.get(1)).getBytesUnsafe());

            switch (key) {
                case CREATE_WALLET:
                    if (!walletService.isWalletDataValid(value)) {
                        logger.info("Invalid request at create wallet operation");
                        return 1;
                    }
                    break;
                case SEND_COINS:
                    if (!coinTransactionService.isTransactionValid(value)) {
                        logger.info("Invalid signature at send coin operation");
                        return 2;
                    }
                    break;
                default:
                    logger.info("Invalid operation key");
                    return 1;
            }
            return 0;
        } catch (Exception e) {
            return 2;
        }
    }

    private List<byte[]> split(ByteString tx, char separator) {
        var arr = tx.toByteArray();
        int i;
        for (i = 0; i < tx.size(); i++) {
            if (arr[i] == (byte) separator) {
                break;
            }
        }
        if (i == tx.size()) {
            return Collections.emptyList();
        }
        return List.of(
                tx.substring(0, i).toByteArray(),
                tx.substring(i + 1).toByteArray()
        );
    }

    private byte[] getPersistedValue(byte[] k) {
        return env.computeInReadonlyTransaction(txn -> {
            var store = env.openStore("store", StoreConfig.WITHOUT_DUPLICATES, txn);
            ByteIterable byteIterable = store.get(txn, new ArrayByteIterable(k));
            if (byteIterable == null) {
                return null;
            }
            return byteIterable.getBytesUnsafe();
        });
    }
}
