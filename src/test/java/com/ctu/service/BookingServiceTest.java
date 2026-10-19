
package com.ctu.service;

import com.ctu.exception.PaymentFailedException;
import com.ctu.exception.RoomNotAvailableException;
import com.ctu.model.BookingReceipt;
import com.ctu.model.Room;
import com.ctu.repository.RoomRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private BookingService bookingService;


    @Test
    @DisplayName("Dat phong thanh cong")
    void testBookRoom_Success() {
        // Arrange
        Room room = new Room("R101", 500000, true);

        when(roomRepository.findById("R101"))
                .thenReturn(Optional.of(room));

        when(paymentGateway.charge("CARD123", 1000000))
                .thenReturn(true);

        // Act
        BookingReceipt receipt =
                bookingService.bookRoom("R101", 2, "CARD123");

        // Assert
        assertNotNull(receipt);
        assertEquals("R101", receipt.getRoomId());
        assertEquals(1000000, receipt.getTotalAmount(), 0.001);
        assertFalse(room.isAvailable());

        verify(paymentGateway, times(1))
                .charge("CARD123", 1000000);

        verify(roomRepository, times(1)).save(room);
    }


    @Test
    @DisplayName("Khong tim thay phong")
    void testBookRoom_RoomNotFound_ThrowsException() {
        when(roomRepository.findById("R999"))
                .thenReturn(Optional.empty());

        RoomNotAvailableException exception =
                assertThrows(
                        RoomNotAvailableException.class,
                        () -> bookingService.bookRoom("R999", 2, "CARD123")
                );

        assertEquals("Khong tim thay phong: R999",
                exception.getMessage());

        verify(roomRepository, never()).save(any());
        verifyNoInteractions(paymentGateway);
    }


    @Test
    @DisplayName("Phong da duoc dat truoc do")
    void testBookRoom_RoomAlreadyBooked_ThrowsException() {
        Room room = new Room("R102", 500000, false);

        when(roomRepository.findById("R102"))
                .thenReturn(Optional.of(room));

        RoomNotAvailableException exception =
                assertThrows(
                        RoomNotAvailableException.class,
                        () -> bookingService.bookRoom("R102", 1, "CARD123")
                );

        assertEquals("Phong da duoc dat truoc do: R102",
                exception.getMessage());

        verifyNoInteractions(paymentGateway);
        verify(roomRepository, never()).save(any());
    }


    @Test
    @DisplayName("Thanh toan that bai")
    void testBookRoom_PaymentFailed_ThrowsException() {
        Room room = new Room("R103", 500000, true);

        when(roomRepository.findById("R103"))
                .thenReturn(Optional.of(room));

        when(paymentGateway.charge("CARD123", 500000))
                .thenReturn(false);

        PaymentFailedException exception =
                assertThrows(
                        PaymentFailedException.class,
                        () -> bookingService.bookRoom("R103", 1, "CARD123")
                );

        assertEquals(
                "Thanh toan qua the credit card that bai!",
                exception.getMessage()
        );

        assertTrue(room.isAvailable());
        verify(paymentGateway, times(1))
                .charge("CARD123", 500000);
        verify(roomRepository, never()).save(any());
    }


    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("So dem khong hop le")
    void testBookRoom_InvalidNights_ThrowsException(int nights) {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.bookRoom("R101", nights, "CARD123")
        );

        verifyNoInteractions(roomRepository);
        verifyNoInteractions(paymentGateway);
    }


    @ParameterizedTest
    @ValueSource(strings = {""})
    @DisplayName("The thanh toan rong")
    void testBookRoom_EmptyCreditCard_ThrowsException(String card) {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.bookRoom("R101", 1, card)
        );

        verifyNoInteractions(roomRepository);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("The thanh toan null")
    void testBookRoom_NullCreditCard_ThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.bookRoom("R101", 1, null)
        );

        verifyNoInteractions(roomRepository);
        verifyNoInteractions(paymentGateway);
    }


    @Test
    @DisplayName("Ma phong null")
    void testBookRoom_NullRoomId_ThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.bookRoom(null, 1, "CARD123")
        );

        verifyNoInteractions(roomRepository);
        verifyNoInteractions(paymentGateway);
    }

}
