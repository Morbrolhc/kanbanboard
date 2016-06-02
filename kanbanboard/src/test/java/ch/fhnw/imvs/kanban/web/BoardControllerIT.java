/*
 * MIT License
 *
 * Copyright (c) 2016 Maurice Gschwind
 * Copyright (c) 2016 Samuel Merki
 * Copyright (c) 2016 Joel Wasmer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ch.fhnw.imvs.kanban.web;

import ch.fhnw.imvs.kanban.SpringIntegrationTest;
import ch.fhnw.imvs.kanban.dto.BoardCreateDto;
import ch.fhnw.imvs.kanban.dto.BoardUpdateDto;
import ch.fhnw.imvs.kanban.dto.UserDto;
import ch.fhnw.imvs.kanban.model.Board;
import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.persistence.BoardRepository;
import ch.fhnw.imvs.kanban.persistence.TaskRepository;
import ch.fhnw.imvs.kanban.persistence.UserRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class BoardControllerIT extends SpringIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(BoardControllerIT.class);

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Override
    public void setUp() {

    }

    @Override
    public void tearDown() {

    }


    @Test
    public void createBoardTest() throws Exception {
        final String token = createUserToken();

        log.debug("Token: " + token);

        BoardCreateDto boardCreateDto = new BoardCreateDto("NewBoard");

        mockMvc.perform(post(PREFIX + "/boards").contentType(contentType).header("Cookie", token).content(this.json(boardCreateDto)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));

        final User owner = userRepository.findByUsername("user");
        final List<Board> boardList = boardRepository.findByName("NewBoard");
        assertNotNull(boardList);
        Board createdBoard = boardList.get(0);
        assertEquals("NewBoard", createdBoard.getName());
        assertEquals(owner.getId(), createdBoard.getOwner().getId());
        assertEquals(owner.getId(), createdBoard.getUsers().stream().findFirst().get().getId());
        assertEquals(1, createdBoard.getUsers().size());
    }

    @Test
    @DirtiesContext
    public void findAllTest() throws Exception {
        final String userToken = createUserToken();
        final String adminToken = createAdminToken();
        User user = userRepository.findByUsername("user");
        Set<Board> set = new HashSet<>();
        Board b1 = new Board("Board1");
        Board b2 = new Board("Board2");
        b1.setId(new ObjectId("5738401f0cf2ce5f1a97fa0e"));
        b2.setId(new ObjectId("5738401f0cf2ce5f1a97fa10"));
        b1.setOwner(user);
        b2.setOwner(user);
        set.add(b1);
        set.add(b2);

        boardRepository.save(set);

        mockMvc.perform(get(PREFIX + "/boards").header("Cookie", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$[?(@.id == \'5738401f0cf2ce5f1a97fa0e\' && @.name == \'Board1\')]").exists())
                .andExpect(jsonPath("$[?(@.id == \'5738401f0cf2ce5f1a97fa10\' && @.name == \'Board2\')]").exists())
                .andReturn();

        mockMvc.perform(get(PREFIX + "/boards").header("Cookie", userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext
    public void updateBoardTest() throws Exception {
        final String token = createUserToken();
        final String id = "5738401f0cf2ce5f1a97fa0e";

        User user = userRepository.findByUsername("user");
        Board b1 = new Board("Board1");
        b1.setId(new ObjectId(id));
        b1.setOwner(user);
        boardRepository.save(b1);

        Set<UserDto> newUsers = new LinkedHashSet<>();
        User user2 = userRepository.findByUsername("user2");
        newUsers.add(new UserDto(user2));

        BoardUpdateDto updateDto = new BoardUpdateDto("SuperNewBoardName", new UserDto(user), newUsers);

        mockMvc.perform(put(PREFIX + "/boards/" + id).contentType(contentType).header("Cookie", token).content(this.json(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType));

        Board boardResult = boardRepository.findOne(id);

        assertEquals("SuperNewBoardName", boardResult.getName() );
        assertTrue(user2.equals(boardResult.getUsers().iterator().next()));

        mockMvc.perform(put(PREFIX + "/boards/5738401f0cf2ce5f1a97fa10").contentType(contentType).header("Cookie", token).content(this.json(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext
    public void findOneTest() throws Exception {
        final String token = createUserToken();
        final String id = "5738401f0cf2ce5f1a97fa0e";

        User user = userRepository.findByUsername("user");
        Board b1 = new Board("Board1");
        b1.setId(new ObjectId(id));
        b1.setOwner(user);
        boardRepository.save(b1);

        mockMvc.perform(get(PREFIX + "/boards/" + id).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id").value("5738401f0cf2ce5f1a97fa0e"))
                .andExpect(jsonPath("$.name").value("Board1"))
                .andExpect(jsonPath("$.owner.username").value("user"))
                .andReturn();

        mockMvc.perform(get(PREFIX + "/boards/5738401f0cf2ce5f1a97fa10").header("Cookie", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext
    public void deleteBoardTest() throws Exception {
        final String token = createUserToken();
        final String id = "5738401f0cf2ce5f1a97fa0e";
        final String id2 = "5738401f0cf2ce5f1a97fa10";


        User user = userRepository.findByUsername("user");
        Board b1 = new Board("Board1");
        b1.setId(new ObjectId(id));
        b1.setOwner(user);
        Task task = new Task("Test", Task.Category.DOING);
        task.setCreator(user);
        task.setId(new ObjectId(id2));
        b1.addTask(task);
        taskRepository.save(task);
        boardRepository.save(b1);

        mockMvc.perform(delete(PREFIX + "/boards/" + id).header("Cookie", token))
                .andExpect(status().isOk());

        assertNull(boardRepository.findOne(id));
        assertNull(taskRepository.findOne(id2));
    }

    @Test
    @DirtiesContext
    public void addUserToBoard() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 02");
        assertEquals(1, boards.size());
        Board board = boards.get(0);

        int numMmbers = board.getUsers().size();

        assertEquals(board.getOwner().getUsername(), "user");

        UserDto newUser = new UserDto(userRepository.findByUsername("user2"));

        log.debug("Request: {}", this.json(newUser));

        final MvcResult result = mockMvc.perform(put(PREFIX + "/boards/" + board.getId() + "/members/")
                .header("Cookie", token).contentType(contentType).content(this.json(newUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andReturn();

        log.debug("Response: {}", result.getResponse().getContentAsString());

        List<Board> boardsAfter = boardRepository.findByName("testboard 02");
        assertNotNull(boardsAfter);
        assertEquals(1, boardsAfter.size());
        Board boardAfter = boardsAfter.get(0);

        for (User u : boardAfter.getUsers()){
            log.debug("User: {}", u.toString());
        }

        assertEquals(numMmbers+1, boardAfter.getUsers().size());
    }

    @Test
    @DirtiesContext
    public void addUserToBoardUnknownUser() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 02");
        assertEquals(1, boards.size());
        Board board = boards.get(0);

        int numMmbers = board.getUsers().size();

        assertEquals(board.getOwner().getUsername(), "user");

        UserDto newUser = new UserDto("someName", "someDisplayname", "somename@example.com");


        log.debug("Request: {}", this.json(newUser));

        final MvcResult result = mockMvc.perform(put(PREFIX + "/boards/" + board.getId() + "/members/")
                .header("Cookie", token).contentType(contentType).content(this.json(newUser)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType))
                .andReturn();

        log.debug("Response: {}", result.getResponse().getContentAsString());

        List<Board> boardsAfter = boardRepository.findByName("testboard 02");
        assertNotNull(boardsAfter);
        assertEquals(1, boardsAfter.size());
        Board boardAfter = boardsAfter.get(0);

        for (User u : boardAfter.getUsers()){
            log.debug("User: {}", u.toString());
        }

        assertEquals(numMmbers, boardAfter.getUsers().size());
    }

    @Test
    @DirtiesContext
    public void addUserToBoardTwice() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 02");
        assertEquals(1, boards.size());
        Board board = boards.get(0);

        int numMmbers = board.getUsers().size();

        assertEquals(board.getOwner().getUsername(), "user");

        UserDto newUser = new UserDto(userRepository.findByUsername("user2"));

        log.debug("Request: {}", this.json(newUser));

        final MvcResult result = mockMvc.perform(put(PREFIX + "/boards/" + board.getId() + "/members/")
                .header("Cookie", token).contentType(contentType).content(this.json(newUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andReturn();

        final MvcResult result2 = mockMvc.perform(put(PREFIX + "/boards/" + board.getId() + "/members/")
                .header("Cookie", token).contentType(contentType).content(this.json(newUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andReturn();

        log.debug("Response: {}", result.getResponse().getContentAsString());

        assertEquals(result.getResponse().getContentAsString(), result2.getResponse().getContentAsString());

        List<Board> boardsAfter = boardRepository.findByName("testboard 02");
        assertNotNull(boardsAfter);
        assertEquals(1, boardsAfter.size());
        Board boardAfter = boardsAfter.get(0);

        for (User u : boardAfter.getUsers()){
            log.debug("User: {}", u.toString());
        }

        assertEquals(numMmbers+1, boardAfter.getUsers().size());


    }


    @Test
    @DirtiesContext
    public void removeUserFromBoard() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 02");
        assertEquals(1, boards.size());
        Board board = boards.get(0);

        int numMmbers = board.getUsers().size();
        assertTrue(board.getUsers().size() > 0);

        assertEquals(board.getOwner().getUsername(), "user");

        String user = board.getUsers().stream().findFirst().get().getDisplayname();


        final MvcResult result = mockMvc.perform(delete(PREFIX + "/boards/" + board.getId() + "/members/" + user)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andReturn();

        log.debug("Response: {}", result.getResponse().getContentAsString());

        List<Board> boardsAfter = boardRepository.findByName("testboard 02");
        assertNotNull(boardsAfter);
        assertEquals(1, boardsAfter.size());
        Board boardAfter = boardsAfter.get(0);

        for (User u : boardAfter.getUsers()){
            log.debug("User: {}", u.toString());
        }

        assertEquals(numMmbers-1, boardAfter.getUsers().size());
    }

    @Test
    @DirtiesContext
    public void removeUserFromBoardTwice() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 02");
        assertEquals(1, boards.size());
        Board board = boards.get(0);

        int numMmbers = board.getUsers().size();
        assertTrue(board.getUsers().size() > 0);

        assertEquals(board.getOwner().getUsername(), "user");

        // Just get a random user
        String user = board.getUsers().stream().findFirst().get().getDisplayname();



        final MvcResult result = mockMvc.perform(delete(PREFIX + "/boards/" + board.getId() + "/members/" + user)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andReturn();

        final MvcResult result2 = mockMvc.perform(delete(PREFIX + "/boards/" + board.getId() + "/members/" + user)
                .header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andReturn();

        log.debug("Response: {}", result.getResponse().getContentAsString());

        assertEquals(result.getResponse().getContentAsString(), result2.getResponse().getContentAsString());

        List<Board> boardsAfter = boardRepository.findByName("testboard 02");
        assertNotNull(boardsAfter);
        assertEquals(1, boardsAfter.size());
        Board boardAfter = boardsAfter.get(0);

        for (User u : boardAfter.getUsers()){
            log.debug("User: {}", u.toString());
        }

        assertEquals(numMmbers-1, boardAfter.getUsers().size());
    }

    @Test
    @DirtiesContext
    public void changeBoardOwner() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 02");
        assertEquals(1, boards.size());
        Board board = boards.get(0);

        User owner = board.getOwner();
        User newOwner = userRepository.findByUsername("userDemo0");

        assertNotEquals(owner, newOwner);

        UserDto newOwnerDto = new UserDto(newOwner);

        final MvcResult result = mockMvc.perform(put(PREFIX + "/boards/" + board.getId() + "/owner/")
                .header("Cookie", token).contentType(contentType).content(this.json(newOwnerDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andReturn();

        log.debug("Response: {}", result.getResponse().getContentAsString());

        final Board boardAfter = boardRepository.findByName("testboard 02").get(0);

        assertEquals(newOwner, boardAfter.getOwner());
    }

    @Test
    @DirtiesContext
    public void changeBoardOwnerNotAMember() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 02");
        assertEquals(1, boards.size());
        Board board = boards.get(0);

        User owner = board.getOwner();
        User newOwner = userRepository.findByUsername("user2");

        assertNotEquals(owner, newOwner);

        UserDto newOwnerDto = new UserDto(newOwner);

        final MvcResult result = mockMvc.perform(put(PREFIX + "/boards/" + board.getId() + "/owner/")
                .header("Cookie", token).contentType(contentType).content(this.json(newOwnerDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType))
                .andReturn();

        log.debug("Response: {}", result.getResponse().getContentAsString());

        final Board boardAfter = boardRepository.findByName("testboard 02").get(0);

        assertEquals(owner, boardAfter.getOwner());
    }

}

