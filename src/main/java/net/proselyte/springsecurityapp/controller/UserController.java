package net.proselyte.springsecurityapp.controller;

import net.proselyte.springsecurityapp.model.*;
import net.proselyte.springsecurityapp.service.*;
import net.proselyte.springsecurityapp.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Controller for {@link net.proselyte.springsecurityapp.model.User}'s pages.
 *
 */

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;
	
	@Autowired
    private BookService bookService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserBookBalaceService userBookBalanceService;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(Model model) {
        model.addAttribute("userForm", new User());

        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult, Model model) {
        userValidator.validate(userForm, bindingResult, true);

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userService.save(userForm);

        securityService.autoLogin(userForm.getUsername(), userForm.getConfirmPassword());

        return "redirect:/welcome";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("error", "Username or password is incorrect.");
        }

        if (logout != null) {
            model.addAttribute("message", "Logged out successfully.");
        }

        return "login";
    }

    @RequestMapping(value = {"/", "/welcome"}, method = RequestMethod.GET)
    public String welcome(Model model, @RequestParam(value = "page", required=false) Integer page) {
        if (page == null) {
            page = 0;
        }
        model.addAttribute("books", bookService.findAllByOrderByNameAsc(page, 10));
        return "welcome";
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String admin(Model model,
                        @RequestParam(value = "usersPage", required=false) Integer usersPage,
                        @RequestParam(value = "booksPage", required=false) Integer booksPage,
                        @RequestParam(value = "booksSearch", required=false) String booksSearch) {

        boolean notFull = false;

        if (usersPage == null) {
            usersPage = 1;
            notFull = true;
        }

        if (booksPage == null) {
            booksPage = 1;
            notFull = true;
        }

        if (booksSearch == null) {
            booksSearch = "";
            notFull = true;
        }

        if (notFull) {
            return "redirect:/admin?booksPage=" + booksPage + "&usersPage=" + usersPage + "&booksSearch=" + booksSearch;
        }

        int booksCount = (int) bookService.countByNameContainingIgnoreCase(booksSearch);
        int booksCountPages = booksCount / 5 + (booksCount % 5 > 0 ? 1 : 0);
        if (booksCountPages < 1) {
            booksCountPages = 1;
        }
        if (booksPage < 1) {
            booksPage = 1;
        }
        if (booksPage > booksCountPages) {
            booksPage = booksCountPages;
        }
        List<Book> books = bookService.findByNameContainingIgnoreCaseOrderByNameAsc(booksSearch, booksPage - 1, 5);
        model.addAttribute("books", books);

        int usersCount = (int) userService.count();
        int usersCountPages = usersCount / 5 + (usersCount % 5 > 0 ? 1 : 0);
        if (usersPage < 1) {
            usersPage = 1;
        }
        if (usersPage > usersCountPages) {
            usersPage = usersCountPages;
        }
        List<User> users = userService.findAllByOrderByIdAsc(usersPage - 1, 5);
        model.addAttribute("users", users);

        model.addAttribute("booksPageContr", new PageController(booksCountPages, booksPage));
        model.addAttribute("usersPageContr", new PageController(usersCountPages, usersPage));
        return "admin";
    }

    @RequestMapping(value = "/testdb", method = RequestMethod.GET)
    public String testdb(Model model){
        StringBuilder str = new StringBuilder();

        str.append("Пользователи: ");
        str.append("<br>");
        for (User user : userService.findAll()) {
            str.append(user.getUsername());
            str.append("<br>");
        }
        str.append("<br>");

        str.append("Книги: ");
        str.append("<br>");
        for (Book book : bookService.findAll()) {
            str.append(book.getName());
            str.append(" : ");
            str.append(book.getDescription());
            str.append(" : ");
            str.append(book.getDate().toString());
            str.append("<br>");
        }
        str.append("<br>");

        str.append("История: ");
        str.append("<br>");
        for (History hist : historyService.findAll()) {
            str.append(hist.getUser_id());
            str.append(" : ");
            str.append(hist.getBook_id());
            str.append(" : ");
            str.append(hist.getAction_type());
            str.append(" : ");
            str.append(hist.getDate().toString());
            str.append("<br>");
        }
        str.append("<br>");

        str.append("Комментарии: ");
        str.append("<br>");
        for (Comment comment : commentService.findAll()) {
            str.append(comment.getBook_id());
            str.append(" : ");
            str.append(comment.getUser_id());
            str.append(" : ");
            str.append(comment.getText());
            str.append(" : ");
            str.append(comment.getTime().toString());
            str.append("<br>");
        }
        str.append("<br>");

        model.addAttribute("log", str.toString());
        return "testdb";
    }

    @RequestMapping(value = "/personal", method = RequestMethod.GET)
    public String personal(Model model){
        List<Book> books = userBookBalanceService.findActiveBooks(getCurrentUser().getId());

        model.addAttribute("userForm", new User());
        model.addAttribute("books", books);
        return "personal";
    }

    @RequestMapping(value = "/personal", method = RequestMethod.POST)
    public String personal(Model model,
                           @RequestParam(value = "removeBookId", required=false) Long removeBookId,
                           @ModelAttribute("userForm") User userForm,
                           BindingResult bindingResult) {

        if (removeBookId != null) {
            History item = new History();
            item.setUser_id(getCurrentUser().getId());
            item.setBook_id(removeBookId);
            item.setAction_type(1l);
            item.setDate(new Date(System.currentTimeMillis()));

            historyService.save(item);
        }

        if (userForm.getPassword() != null) {
            userValidator.validate(userForm, bindingResult, false);
            if (bindingResult.hasErrors()) {
                List<Book> books = userBookBalanceService.findActiveBooks(getCurrentUser().getId());
                model.addAttribute("books", books);
                return "personal";
            }
            User user = getCurrentUser();
            userForm.setId(user.getId());
            userForm.setUsername(user.getUsername());
            userService.save(userForm);
        }

        return "redirect:/personal";
    }

    @RequestMapping(value = "/userpage", method = RequestMethod.GET)
    public String userpage(Model model){
        model.addAttribute("books", bookService.findAll());
        return "userpage";
    }

    private User getCurrentUser() {
        UserDetails tempUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.findByUsername(tempUser.getUsername());
        return currentUser;
    }
}
