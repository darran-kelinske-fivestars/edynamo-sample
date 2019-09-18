package com.fivestars.edynamosample;

public final class TransactionStatusEvent {
    final static int NO_EVENTS = 0x00;
    final static int CARD_INSERTED = 0x01;
    final static int CARD_ERROR = 0x02;
    final static int TRANSACTION_PROGRESS_CHANGE = 0x03;
    final static int WAITING_FOR_USER_RESPONSE = 0x04;
    final static int TIMEOUT = 0x05;
    final static int TRANSACTION_TERMINATED = 0x06;
    final static int HOST_CANCELLED_TRANSACTION = 0x07;
    final static int CARD_REMOVED = 0x08;
}
