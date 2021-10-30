package com.wrox.site.controller;

import com.wrox.config.annotation.WebController;
import com.wrox.site.entities.*;
import com.wrox.site.services.*;
import com.wrox.site.validation.Name;
import com.wrox.site.validation.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

@WebController
//@RequestMapping(value = "admin")
public class AdminController {
    @Inject
    UserPrincipalService userPrincipalService;
    @Inject
    ProfileService profileService;
    @Inject
    EventService eventService;
    @Inject
    CategoryService categoryService;
    @Inject
    EventStatusService eventStatusService;

    public static class SignupFrom{
        @NotBlank
        @NotNull
        String username;
        @NotBlank
        @NotNull
        String password;

    public SignupFrom() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

    @RequestMapping(value = "signup", method = RequestMethod.POST)
    public String signup(@Valid SignupFrom signupFrom,
                         Errors errors,
                         Map<String, Object> model){
        if(errors.hasErrors()){
            model.put("signupMessage", "Username and password must not be blank");
            return "signup";
        }

        UserPrincipal user = userPrincipalService.loadUserByUsername(signupFrom.username);
        if(user!=null){
            model.put("signupMessage", "Duplicate username");
            return "signup";
        }
        user = new UserPrincipal();
        user.setUsername(signupFrom.username);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        userPrincipalService.saveUser(user, signupFrom.password, "Administrator");
        model.put("signupMessage", "Success");
        return "signup";
    }

    @RequestMapping(value = "signup", method = RequestMethod.GET)
    public String signup(){
        return "signup";
    }

    @RequestMapping(value = "admin/accounts", method = RequestMethod.GET)
    public String getAccounts(ModelMap model,@PageableDefault Pageable p,
                              @RequestParam(required = false) Boolean enable){
        Page<UserPrincipal> resultPage;
        if(enable==null)
            resultPage = userPrincipalService.loadAllUser(p);
        else
            resultPage = userPrincipalService.loadUsers(p, enable);
        model.put("accountPage", resultPage);
        return "admin/accounts";
    }
    @RequestMapping(value = "admin/findAccount", method = RequestMethod.GET)
    public String findAccount(ModelMap model,@PageableDefault Pageable p,
                              @RequestParam String name){
        Page<UserPrincipal> resultPage = userPrincipalService.loadUserByUsername(name,p);
        model.put("accountPage", resultPage);
        return "admin/accounts";
    }
    @RequestMapping(value = "admin/switchAccountState", method = RequestMethod.GET)
    public String switchAccountState(@RequestParam long userId,
                                           ModelMap model,
                                     @PageableDefault Pageable p){
        UserPrincipal user = userPrincipalService.switchState(userId) ;
        return findAccount(model, p, user.getUsername());
    }

    @RequestMapping(value = "admin/events", method = RequestMethod.GET)
    public String getAllEvents(ModelMap model,@PageableDefault Pageable p){
        model.put("eventPage", eventService.gettAllEvent(p));
        return "admin/events";
    }

    @RequestMapping(value = "admin/getEventsByOrganizer", method = RequestMethod.GET)
    public String searchEventByOrg(ModelMap model, @RequestParam Long organizerId,
                                   @PageableDefault Pageable p){

        model.put("eventPage", eventService.getAllEvents(p, organizerId));
        return "admin/events";
    }

    @RequestMapping(value = "admin/getEventById", method = RequestMethod.GET)
    public String search(@RequestParam long eventId,
                         ModelMap model){
        model.put("eventDetail", eventService.getEventDetail(eventId));
        return "admin/events";
    }

    @RequestMapping(value = "admin/searchEvent", method = RequestMethod.GET)
    public String searchEvent(ModelMap model, EventController.EventSearchForm form,
                              @PageableDefault Pageable p){
        if(form.startDate==null && form.title == null && form.endDate==null)
            return getAllEvents(model, p);
        Set<Category> categorySet = null;
        if(form.categories!=null){
            categorySet = categoryService.getByIds(form.categories, true);
        }
        Page<Event> eventPage = eventService.searchEvent(form.title,categorySet,form.organizers,
                form.tags, form.startDate,form.endDate, p);
        model.put("eventPage", eventPage);
        return "admin/events";
    }

    @RequestMapping(value = "admin/deleteEvent", method = RequestMethod.GET)
    public String deleteEvent(@RequestParam long eventId,
                              ModelMap model,
                              @PageableDefault Pageable p){
        Event deletedEvent = eventService.getEventDetail(eventId);
        if(deletedEvent!=null){
            EventStatus deleteStat = eventStatusService.getStatus("Deleted");
            deletedEvent.setStatus(deleteStat);
            eventService.saveEvent(deletedEvent);
            return searchEventByOrg(model, deletedEvent.getUserProfileId(), null);
        }
        return getAllEvents(model, p);
    }


    @RequestMapping(value = "admin/addCategory", method = RequestMethod.GET)
    public String addCategory(@Valid CategoryForm form,
                              Errors errors,
                              ModelMap model){
        boolean valid = true;
        if(errors.hasErrors()){
            model.put("addCategoryError", "Invalid name for category");
            valid = false;
        }
        if(categoryService.getByName(form.categoryName)!=null){
            model.put("addCategoryError", "Duplicate category name");
            valid = false;
        }
        if(valid){
            Category newCategory = new Category();
            newCategory.setStatus(true);
            newCategory.setName(form.categoryName);
            categoryService.save(newCategory);
        }
        return listCategories(model);
    }

    @RequestMapping(value = "admin/categories", method = RequestMethod.GET)
    public String listCategories(ModelMap model){
        model.addAttribute("categories", categoryService.getAll());
        return "admin/categories";
    }

    @RequestMapping(value = "admin/switchCategoryState", method = RequestMethod.GET)
    public String switchCategoryState(@RequestParam long categoryId,
                                      ModelMap model){
        Category cat = categoryService.getById(categoryId);
        if(cat!=null){
            cat.setStatus(!cat.isStatus());
            categoryService.save(cat);
        }
        return listCategories(model);
    }


    @RequestMapping(value = "admin/login", method = RequestMethod.GET)
    public ModelAndView login(Map<String, Object> model, @AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        if(userPrincipal!=null &&
                userPrincipal.getAuthorities().stream().anyMatch(r -> "Administrator".equals(r.getAuthority())))
            return new ModelAndView(new RedirectView("/admin/accounts", true, false));

        model.put("loginForm", new LoginForm());
        return new ModelAndView("login");
    }

    public static class LoginForm
    {
        private String username;
        private String password;

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }
    }

    public static class CategoryForm{
        @Name
        @NotNull
        String categoryName;

        public CategoryForm() {
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
    }

}
