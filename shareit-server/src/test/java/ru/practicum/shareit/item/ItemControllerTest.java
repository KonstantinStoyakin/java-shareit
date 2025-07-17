package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemMapper itemMapper;

    private ItemDto itemDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Test Comment");
        commentDto.setAuthorName("Author");
        commentDto.setCreated(LocalDateTime.now());
    }

    @Test
    void addItem_shouldReturnCreatedItem() throws Exception {
        Item item = new Item();
        Mockito.when(itemMapper.toItem(any(ItemDto.class))).thenReturn(item);
        Mockito.when(itemService.addItem(any(Item.class), anyLong())).thenReturn(item);
        Mockito.when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void addItem_shouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        Item item = new Item();
        Mockito.when(itemMapper.toItem(any(ItemDto.class))).thenReturn(item);
        Mockito.when(itemService.updateItem(any(Item.class), anyLong())).thenReturn(item);
        Mockito.when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Item\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getItem_shouldReturnItem() throws Exception {
        Item item = new Item();
        Mockito.when(itemService.getItem(anyLong(), anyLong())).thenReturn(item);
        Mockito.when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserItems_shouldReturnListOfItems() throws Exception {
        Item item = new Item();
        Mockito.when(itemService.getUserItems(anyLong(), anyInt(), anyInt())).thenReturn(List.of(item));
        Mockito.when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void searchItems_shouldReturnMatchingItems() throws Exception {
        Item item = new Item();
        Mockito.when(itemService.searchItems(anyString())).thenReturn(List.of(item));
        Mockito.when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        Comment comment = new Comment();
        Mockito.when(itemService.addComment(any(Comment.class), anyLong(), anyLong())).thenReturn(comment);
        Mockito.when(itemMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Test Comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Test Comment"));
    }

    @Test
    void addComment_shouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(containsString("Comment text cannot be empty")));
    }

    @Test
    void searchItems_shouldReturnEmptyOnEmptyText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}