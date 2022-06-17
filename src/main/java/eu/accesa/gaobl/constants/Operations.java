package eu.accesa.gaobl.constants;

import java.util.List;

public interface Operations {

    String CREATE_WALLET = "create-wallet";
    String SEND_COINS = "send-coins";

    List<String> availableOperations = List.of(CREATE_WALLET, SEND_COINS);
}
