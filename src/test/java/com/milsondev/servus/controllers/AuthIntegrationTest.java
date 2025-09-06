package com.milsondev.servus.controllers;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.db.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity testUser;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        //testUser.setFullName("User Test");
        testUser.setPhone("1234567890");
        userRepository.save(testUser);
    }

    @Test
    public void getLoginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void getRegisterPage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "sign-up");
    }

    @Test
    public void getPasswordResetPage() throws Exception {
        mockMvc.perform(get("/auth/password-reset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/password-reset"));
    }

    @Test
    public void successfulLogin() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "test@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments"))
                .andExpect(cookie().exists("Authorization"));
    }

    @Test
    public void failedLogin() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "test@example.com")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "login");
        //ModelAndViewAssert.assertModelAttributeExists(mvcResult.getModelAndView(), "errors");
    }

    @Test
    public void successfulLogout() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge("Authorization", 0));
    }

    @Test
    public void successfulRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("email", "newuser@example.com")
                        .param("password", "password123")
                        .param("phoneNumber", "0987654321"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-up/success"));
    }

    @Test
    public void registrationWithExistingEmail() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("email", "test@example.com")
                        .param("password", "password")
                        .param("phoneNumber", "1234567890"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndViewAssert.assertViewName(mvcResult.getModelAndView(), "sign-up");
        //ModelAndViewAssert.assertModelAttributeExists(mvcResult.getModelAndView(), "errors");
    }
}