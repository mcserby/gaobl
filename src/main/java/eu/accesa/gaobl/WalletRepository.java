package eu.accesa.gaobl;

import eu.accesa.gaobl.dto.Wallet;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class WalletRepository {

    public void saveWallet(Wallet wallet) {
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance("tmp/storage-entity")) {
            entityStore.executeInTransaction(txn -> {
                final Entity message = txn.newEntity("Wallet");
                message.setProperty("id", wallet.getId());
                message.setProperty("amount", wallet.getAmount());
                message.setProperty("publicKey", wallet.getPublicKey());
                message.setProperty("nickname", wallet.getNickname());
            });
        }
    }

    public void updateWallet(Wallet wallet) {
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance("tmp/storage-entity")) {
            entityStore.executeInTransaction(txn -> {
                Entity entity = txn.find("Wallet", "id", wallet.getId()).getFirst();
                entity.setProperty("amount", wallet.getAmount());
                txn.saveEntity(entity);
            });
        }
    }

    public List<Wallet> findWallets() {
        List<Wallet> walletList = new LinkedList<>();
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance("tmp/storage-entity")) {
            entityStore.executeInTransaction(txn -> {
                EntityIterable wallets = txn.getAll("Wallet");
                for (Entity entity : wallets) {
                    Wallet readWallet = new Wallet();
                    readWallet.setId(Objects.requireNonNull(entity.getProperty("id")).toString());
                    readWallet.setAmount(Integer.parseInt(Objects.requireNonNull(entity.getProperty("amount")).toString()));
                    readWallet.setPublicKey(Objects.requireNonNull(entity.getProperty("publicKey")).toString());
                    readWallet.setNickname(Objects.requireNonNull(entity.getProperty("nickname")).toString());
                    walletList.add(readWallet);
                }
            });
        }
        return walletList;
    }

    public boolean walletExistsForPublicKey(Comparable publicKey) {
        AtomicBoolean walletExistsForOwner = new AtomicBoolean(false);
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance("tmp/storage-entity")) {
            entityStore.executeInTransaction(txn -> {
                EntityIterable wallets = txn.find("Wallet", "publicKey", publicKey);
                if(wallets.size()>0) {
                    walletExistsForOwner.set(true);
                }
            });
        }
        return walletExistsForOwner.get();
    }

    public Wallet findWalletById(Comparable walletId) {
        Wallet wallet = new Wallet();
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance("tmp/storage-entity")) {
            entityStore.executeInTransaction(txn -> {
                EntityIterable wallets = txn.find("Wallet", "id", walletId);
                if(wallets.size() == 0) {
                    return;
                }
                for (Entity entity : wallets) {
                    wallet.setId(Objects.requireNonNull(entity.getProperty("id")).toString());
                    wallet.setAmount(Integer.parseInt(Objects.requireNonNull(entity.getProperty("amount")).toString()));
                    wallet.setPublicKey(Objects.requireNonNull(entity.getProperty("publicKey")).toString());
                    wallet.setNickname(Objects.requireNonNull(entity.getProperty("nickname")).toString());
                }
            });
        }
        return wallet;
    }

}
