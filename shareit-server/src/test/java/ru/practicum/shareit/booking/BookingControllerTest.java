package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private BookingMapper bookingMapper;

    private BookingDto bookingDto;
    private BookingResponseDto responseDto;

    @BeforeEach
    void setUp() {
        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(1L);

        UserDto bookerDto = new UserDto();
        bookerDto.setId(1L);
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker@example.com");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStart(LocalDateTime.now().plusDays(1));
        responseDto.setEnd(LocalDateTime.now().plusDays(2));
        responseDto.setStatus(BookingStatus.WAITING);
        responseDto.setBooker(bookerDto);
        responseDto.setItem(itemDto);
    }

    @Test
    void addBooking_shouldReturnCreatedBooking() throws Exception {
        Mockito.when(bookingMapper.toBooking(any(BookingDto.class))).thenReturn(new Booking());
        Mockito.when(bookingService.addBooking(any(Booking.class), anyLong())).thenReturn(new Booking());
        Mockito.when(bookingMapper.toResponseDto(any(Booking.class))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void addBooking_shouldReturn404WhenItemNotFound() throws Exception {
        Mockito.when(bookingMapper.toBooking(any(BookingDto.class))).thenReturn(new Booking());
        Mockito.when(bookingService.addBooking(any(Booking.class), anyLong()))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBooking_shouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBooking_shouldReturn400WhenInvalidDates() throws Exception {
        bookingDto.setStart(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_shouldReturnApprovedBooking() throws Exception {
        responseDto.setStatus(BookingStatus.APPROVED);

        Mockito.when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(new Booking());
        Mockito.when(bookingMapper.toResponseDto(any(Booking.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveBooking_shouldReturn404WhenBookingNotFound() throws Exception {
        Mockito.when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveBooking_shouldReturn403WhenNotOwner() throws Exception {
        Mockito.when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ForbiddenException("Only owner can approve booking"));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approveBooking_shouldReturn400WhenAlreadyApproved() throws Exception {
        Mockito.when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new ValidationException("Booking already approved"));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_shouldReturnBooking() throws Exception {
        Mockito.when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(new Booking());
        Mockito.when(bookingMapper.toResponseDto(any(Booking.class))).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getBooking_shouldReturn404WhenBookingNotFound() throws Exception {
        Mockito.when(bookingService.getBooking(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBooking_shouldReturn403WhenNotOwnerOrBooker() throws Exception {
        Mockito.when(bookingService.getBooking(anyLong(), anyLong()))
                .thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserBookings_shouldReturnListOfBookings() throws Exception {
        Mockito.when(bookingService.getUserBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(new Booking()));
        Mockito.when(bookingMapper.toResponseDto(any(Booking.class))).thenReturn(responseDto);

        mockMvc.perform(get("/bookings?state=ALL&from=0&size=10")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getUserBookings_shouldReturn400ForInvalidState() throws Exception {
        mockMvc.perform(get("/bookings?state=INVALID&from=0&size=10")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_shouldReturn400ForInvalidPagination() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=-1&size=0")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_shouldReturnListOfBookings() throws Exception {
        Mockito.when(bookingService.getOwnerBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(new Booking()));
        Mockito.when(bookingMapper.toResponseDto(any(Booking.class))).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=10")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getOwnerBookings_shouldReturn400ForInvalidState() throws Exception {
        Mockito.when(bookingService.getOwnerBookings(anyLong(), eq("INVALID"), anyInt(), anyInt()))
                .thenThrow(new ValidationException("Unknown state: INVALID"));

        mockMvc.perform(get("/bookings/owner?state=INVALID&from=0&size=10")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_shouldValidateDates() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .content("{\"start\":\"2023-01-01T00:00:00\", \"end\":\"2022-01-01T00:00:00\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_shouldValidateItemId() throws Exception {
        bookingDto.setItemId(null);

        Mockito.when(bookingMapper.toBooking(any(BookingDto.class)))
                .thenThrow(new ValidationException("Item ID is required"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }
}