package com.fivestars.edynamosample;

public final class TransactionStatusProgress {
    final static int NO_TRANSACTION_IN_PROGRESS = 0x00;
    final static int WAITING_FOR_USER_TO_INSERT_CARD = 0x01;
    final static int POWERING_UP_CARD = 0x02;
    final static int SELECTING_APPLICATION = 0x03;
    final static int WAITING_USER_LANGUAGE_SELECTION = 0x04;
    final static int WAITING_USER_APP_SELECTION = 0x05;
    final static int INITIATING_APPLICATION = 0x06;
    final static int READING_APP_DATA = 0x07;
    final static int OFFLINE_DATA_AUTHENTICATION = 0x08;
    final static int PROCESS_RESTRICTIONS = 0x09;
    final static int CARDHOLDER_VERIFICATION = 0x0A;
    final static int TERMINAL_RISK_MANAGEMENT = 0x0B;
    final static int TERMINAL_ACTION_ANALYSIS = 0x0C;
    final static int GENERATING_FIRST_APP_CRYPTOGRAM = 0x0D;
    final static int CARD_ACTION_ANALYSIS = 0x0E;
    final static int ONLINE_PROCESSING = 0x0F;
    final static int WAITING_ONLINE_PROCESSING_RESPONSE = 0x10;
    final static int TRANSACTION_COMPLETION = 0x11;
    final static int TRANSACTION_ERROR = 0x12;
    final static int TRANSACTION_APPROVED = 0x13;
    final static int TRANSACTION_DECLINED = 0x14;
    final static int BUSY = 0x15;
}
