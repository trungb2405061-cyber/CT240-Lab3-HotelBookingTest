
package com.ctu.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingReceiptModelTest {

    @Test
    void testBookingReceiptConstructorAndGetters() {
        // Arrange
        BookingReceipt receipt =
                new BookingReceipt("B001", "R101", 1000000);

        // Act & Assert
        assertEquals("B001", receipt.getBookingId());
        assertEquals("R101", receipt.getRoomId());
        assertEquals(1000000, receipt.getTotalAmount(), 0.001);
    }
}
