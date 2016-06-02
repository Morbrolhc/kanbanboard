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
import ch.fhnw.imvs.kanban.dto.TaskDto;
import ch.fhnw.imvs.kanban.dto.TaskMoveDto;
import ch.fhnw.imvs.kanban.dto.UserDto;
import ch.fhnw.imvs.kanban.model.Board;
import ch.fhnw.imvs.kanban.model.Task;
import ch.fhnw.imvs.kanban.model.User;
import ch.fhnw.imvs.kanban.persistence.BoardRepository;
import ch.fhnw.imvs.kanban.persistence.TaskRepository;
import ch.fhnw.imvs.kanban.persistence.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TaskControllerIT extends SpringIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(TaskControllerIT.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void setUp() {

    }

    @Override
    public void tearDown() {

    }


    @Test
    public void getTaskById() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        log.debug("Result: {}", content);

        ObjectMapper mapper = new ObjectMapper();
        final TaskDto taskDto = mapper.readValue(content, TaskDto.class);

        assertEquals(task.getId().toHexString(), taskDto.getId());
        assertEquals(task.getTitle(), taskDto.getName());
        assertEquals(task.getDescription(), taskDto.getDescription());
        assertEquals(task.getCategory(), taskDto.getState());
    }


    @Test
    public void getTaskByIdNotLoggedIn() throws Exception {

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType))
                .andExpect(status().isUnauthorized())
//                .andExpect(content().contentType(contentType))
                .andReturn();

        // TODO check result code
        String content = mvcResult.getResponse().getContentAsString();
        log.debug("Result: {}", content);

    }


    @Test
    public void getTaskByIdInvalidId() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        String taskId = "invalidId";
        log.debug("TaskId: {}", taskId);

        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType).header("Cookie", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void getTaskByIdInvalidBoard() throws Exception {
        String token = createUserToken();

        String boardId = "invalidId";

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/boards/" + boardId + "/cards/" + taskId)
                .contentType(contentType).header("Cookie", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DirtiesContext
    public void createTask() throws Exception {
        String taskname = "Testtask 112";
        String taskdescription = "321 Description 123";

        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());
        int numTasks = board.getTasks().size();

        TaskDto taskDto = new TaskDto();
        taskDto.setName(taskname);
        taskDto.setDescription(taskdescription);
        taskDto.setDuedate(new Date());
        taskDto.setState(Task.Category.DOING);

        log.debug("Date format: {}", taskDto.getDuedate());

        final MvcResult mvcResult = mockMvc.perform(post(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/")
                .contentType(contentType).content(this.json(taskDto)).header("Cookie", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(taskname))
                .andExpect(jsonPath("$.state").value(Task.Category.DOING.toString()))
                .andExpect(jsonPath("$.description").value(taskdescription))
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());


        List<Board> boardsAfter = boardRepository.findByName("testboard 01");
        assertEquals(1, boardsAfter.size());
        Board boardAfter = boardsAfter.get(0);

        assertEquals(numTasks+1, boardAfter.getTasks().size());
    }

    @Test
    @DirtiesContext
    public void createTaskWithAssigned() throws Exception {
        String taskname = "Testtask 112";
        String taskdescription = "321 Description 123";

        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());
        int numTasks = board.getTasks().size();

        TaskDto taskDto = new TaskDto();
        taskDto.setName(taskname);
        taskDto.setDescription(taskdescription);
        taskDto.setDuedate(new Date());
        taskDto.setState(Task.Category.DOING);

        final Set<UserDto> assignee = board.getUsers().stream()
                .filter(user -> user.getUsername().contains("Demo"))
                .map(UserDto::new)
                .collect(Collectors.toSet());

        taskDto.setAssigned(assignee);


        log.debug("Date format: {}", taskDto.getDuedate());

        final MvcResult mvcResult = mockMvc.perform(post(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/")
                .contentType(contentType).content(this.json(taskDto)).header("Cookie", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(taskname))
                .andExpect(jsonPath("$.state").value(Task.Category.DOING.toString()))
                .andExpect(jsonPath("$.description").value(taskdescription))
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

        ObjectMapper mapper = new ObjectMapper();
        final TaskDto answer = mapper.readValue(mvcResult.getResponse().getContentAsString(), TaskDto.class);

        assertThat(answer.getAssigned(), containsInAnyOrder(assignee.toArray()));


        List<Board> boardsAfter = boardRepository.findByName("testboard 01");
        assertEquals(1, boardsAfter.size());
        Board boardAfter = boardsAfter.get(0);

        assertEquals(numTasks+1, boardAfter.getTasks().size());
    }


    @Test
    @DirtiesContext
    public void createTaskInvalidBoard() throws Exception {
        String taskname = "Testtask 112";
        String taskdescription = "321 Description 123";

        String token = createUserToken();


        TaskDto taskDto = new TaskDto();
        taskDto.setName(taskname);
        taskDto.setDescription(taskdescription);
        taskDto.setDuedate(new Date());
        taskDto.setState(Task.Category.DOING);

        log.debug("Date format: {}", taskDto.getDuedate());

        final MvcResult mvcResult = mockMvc.perform(post(PREFIX + "/boards/" + "0123invalidId" + "/cards/")
                .contentType(contentType).content(this.json(taskDto)).header("Cookie", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DirtiesContext
    public void createTaskNotABoardMember() throws Exception {
        String taskname = "Testtask 112";
        String taskdescription = "321 Description 123";

        String token = createToken("user2", "password2");


        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);

        TaskDto taskDto = new TaskDto();
        taskDto.setName(taskname);
        taskDto.setDescription(taskdescription);
        taskDto.setDuedate(new Date());
        taskDto.setState(Task.Category.DOING);

        log.debug("Date format: {}", taskDto.getDuedate());

        final MvcResult mvcResult = mockMvc.perform(post(PREFIX + "/boards/" + board.getId() + "/cards/")
                .contentType(contentType).content(this.json(taskDto)).header("Cookie", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DirtiesContext
    public void moveTask() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        assertNotEquals(Task.Category.DONE, task.getCategory());

        TaskMoveDto dto = new TaskMoveDto("DONE");

        final MvcResult mvcResult = mockMvc.perform(put(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId + "/category")
                .contentType(contentType).content(this.json(dto)).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DONE"))
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

        final Task after = taskRepository.findOne(taskId);

        assertEquals(Task.Category.DONE, after.getCategory());
    }

    @Test
    @DirtiesContext
    public void moveTaskInvalidCategory() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        Task.Category catOld = task.getCategory();

        TaskMoveDto dto = new TaskMoveDto("INVALID");


        final MvcResult mvcResult = mockMvc.perform(put(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId + "/category")
                .contentType(contentType).content(this.json(dto)).header("Cookie", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

        final Task after = taskRepository.findOne(taskId);

        assertEquals(catOld, after.getCategory());
    }

    @Test
    @DirtiesContext
    public void moveTaskUnauthorizedUser() throws Exception {
        String token = createToken("userDemo4", "passDemo4");

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        Task.Category catOld = task.getCategory();

        TaskMoveDto dto = new TaskMoveDto("DONE");


        final MvcResult mvcResult = mockMvc.perform(put(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId + "/category")
                .contentType(contentType).content(this.json(dto)).header("Cookie", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

        final Task after = taskRepository.findOne(taskId);

        assertEquals(catOld, after.getCategory());
    }

    @Test
    @DirtiesContext
    public void moveTaskInvalidBoard() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        Task.Category catOld = task.getCategory();

        TaskMoveDto dto = new TaskMoveDto("DOING");


        final MvcResult mvcResult = mockMvc.perform(put(PREFIX + "/boards/" + "12345-invalidId" + "/cards/" + taskId + "/category")
                .contentType(contentType).content(this.json(dto)).header("Cookie", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(content().contentType(contentType)).andReturn();

        log.debug("Result: {}", mvcResult.getResponse().getContentAsString());

        final Task after = taskRepository.findOne(taskId);

        assertEquals(catOld, after.getCategory());
    }

    @Test
    @DirtiesContext
    public void getAndModifyTask() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        final MvcResult mvcResult = mockMvc.perform(get(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        log.debug("Result: {}", content);

        ObjectMapper mapper = new ObjectMapper();
        final TaskDto taskDto = mapper.readValue(content, TaskDto.class);

        assertEquals(task.getId().toHexString(), taskDto.getId());
        assertEquals(task.getTitle(), taskDto.getName());
        assertEquals(task.getDescription(), taskDto.getDescription());
        assertEquals(task.getCategory(), taskDto.getState());

        final String newName = "newName";
        final String description = "newDescription";


        final Date oldDuedate = taskDto.getDuedate();

        Calendar c = Calendar.getInstance();
        c.setTime(oldDuedate);
        c.add(Calendar.DATE, 1);
        Date newDuedate = c.getTime();

        taskDto.setName(newName);
        taskDto.setDescription(description);
        taskDto.setDuedate(newDuedate);

        final MvcResult modifyResult = mockMvc.perform(put(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType).content(this.json(taskDto)).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        final TaskDto modifiedTask = mapper.readValue(modifyResult.getResponse().getContentAsString(), TaskDto.class);

        assertEquals(newName, modifiedTask.getName());
        assertEquals(description, modifiedTask.getDescription());
        assertEquals(taskId, modifiedTask.getId());
        assertNotEquals(DateUtils.truncate(oldDuedate, Calendar.DATE), DateUtils.truncate(modifiedTask.getDuedate(), Calendar.DATE));

        final Task modifiedTaskRepo = boardRepository.findByName("testboard 01").get(0).getTasks().stream()
                .filter(task1 -> task1.getId().toHexString().equals(taskId))
                .findFirst().get();

        assertEquals(modifiedTask.getState(), modifiedTaskRepo.getCategory());
        assertEquals(modifiedTask.getName(), modifiedTaskRepo.getTitle());
    }

    @Test
    @DirtiesContext
    public void modifyTaskUnauthorizedUser() throws Exception {
        String token = createToken("userDemo3", "passDemo3");

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);


        final String newName = "newName";
        final String description = "newDescription";


        TaskDto taskDto = new TaskDto(task);
        final Date oldDuedate = taskDto.getDuedate();

        Calendar c = Calendar.getInstance();
        c.setTime(oldDuedate);
        c.add(Calendar.DATE, 1);
        Date newDuedate = c.getTime();

        taskDto.setName(newName);
        taskDto.setDescription(description);
        taskDto.setDuedate(newDuedate);

        final MvcResult modifyResult = mockMvc.perform(put(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType).content(this.json(taskDto)).header("Cookie", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(content().contentType(contentType)).andReturn();



    }

    @Test
    @DirtiesContext
    public void getAndDeleteTask() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        List<Task> tasks =  new ArrayList<>(board.getTasks());

        int numTasks = board.getTasks().size();

        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        final MvcResult getResult = mockMvc.perform(get(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType).header("Cookie", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType)).andReturn();

        String content = getResult.getResponse().getContentAsString();
        log.debug("Result: {}", content);

        ObjectMapper mapper = new ObjectMapper();
        final TaskDto taskDto = mapper.readValue(content, TaskDto.class);

        assertEquals(task.getId().toHexString(), taskDto.getId());
        assertEquals(task.getTitle(), taskDto.getName());
        assertEquals(task.getDescription(), taskDto.getDescription());
        assertEquals(task.getCategory(), taskDto.getState());

        final MvcResult deleteResult = mockMvc.perform(delete(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType).header("Cookie", token))
                .andExpect(status().isNoContent())
                .andReturn();

        final Optional<Task> deletedTask = boardRepository.findByName("testboard 01").get(0).getTasks().stream()
                .filter(task1 -> task1.getId().toHexString().equals(taskId))
                .findFirst();

        assertFalse(deletedTask.isPresent());
        assertEquals(numTasks-1, boardRepository.findByName("testboard 01").get(0).getTasks().size());

    }

    @Test
    @DirtiesContext
    public void deleteTaskNonExistantTask() throws Exception {
        String token = createUserToken();

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        int numTasks = board.getTasks().size();

        final MvcResult deleteResult = mockMvc.perform(delete(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + "invalidTaskId")
                .contentType(contentType).header("Cookie", token))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.result").exists())
                .andReturn();

        log.debug("Result: {}", deleteResult.getResponse().getContentAsString());

        assertEquals(numTasks, boardRepository.findByName("testboard 01").get(0).getTasks().size());
    }

    @Test
    @DirtiesContext
    public void deleteTaskUnathorizedUser() throws Exception {
        String token = createToken("userDemo5", "passDemo5");

        List<Board> boards = boardRepository.findByName("testboard 01");
        assertEquals(1, boards.size());
        Board board = boards.get(0);
        log.debug("BoardId: {}", board.getId());

        User user = userRepository.findByUsername("userDemo5");


        int numTasks = board.getTasks().size();

        List<Task> tasks =  new ArrayList<>(board.getTasks());
        Task task = tasks.get(0);
        String taskId = task.getId().toHexString();
        log.debug("TaskId: {}", taskId);

        assertFalse(board.getUsers().contains(user));
        assertFalse(board.getOwner().equals(user));


        final MvcResult deleteResult = mockMvc.perform(delete(PREFIX + "/boards/" + board.getId().toHexString() + "/cards/" + taskId)
                .contentType(contentType).header("Cookie", token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.result").exists())
                .andReturn();

        log.debug("Result: {}", deleteResult.getResponse().getContentAsString());

        assertEquals(numTasks, boardRepository.findByName("testboard 01").get(0).getTasks().size());
    }


}
