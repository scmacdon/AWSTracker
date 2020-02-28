package com.aws.securingweb;

import com.aws.entities.WorkItem;
import com.aws.jdbc.RetrieveItems;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.aws.jdbc.InjectWorkService;
import com.aws.services.WriteExcel;
import com.aws.services.SendMessages;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class MainController {

    @GetMapping("/")
    public String root() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

   // @GetMapping("/user")
  //  public String userIndex() {
   //     return "index";
    //}


    //This is invoked when we want to build a report
    @RequestMapping(value = "/report", method = RequestMethod.GET)
    @ResponseBody
    String getReport(HttpServletRequest request, HttpServletResponse response) {

        //Get the work item list
        //Get the Logged in User
        org.springframework.security.core.userdetails.User user2 = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = user2.getUsername();
        RetrieveItems ri = new RetrieveItems();
        List<WorkItem> theList =  ri.getItemsDataSQLReport(name);

        WriteExcel writeExcel = new WriteExcel();
        SendMessages sm = new SendMessages();
        java.io.InputStream is = writeExcel.exportExcel(theList);

        try {
            //copyInputStreamToFile(is, testFile);
            sm.SendReport(is);
        }
        catch (Exception e){
            e.getStackTrace();
        }
        return "Report is created";
    }


    //This is invoked when we want to change the value of a work item
    @RequestMapping(value = "/archive", method = RequestMethod.POST)
    @ResponseBody
    String ArchieveWorkItem(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");

        RetrieveItems ri = new RetrieveItems();
        WorkItem item= ri.GetWorkItembyId(id);
        // db.injectDynamoItem(item);
        ri.FlipItemArchive(id );
        return id ;
    }


    //This is invoked when we want to change the value of a work item
    @RequestMapping(value = "/changewi", method = RequestMethod.POST)
    @ResponseBody
    String ChangeWorkItem(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        String description = request.getParameter("description");
        String status   = request.getParameter("status");

        InjectWorkService ws = new InjectWorkService();
        return ws.modifySubmission(id, description, status) ;
    }

    //This is invoked when we retrieve all items for a given writer
    @RequestMapping(value = "/retrieve", method = RequestMethod.GET)
    @ResponseBody
    String retrieveItems(HttpServletRequest request, HttpServletResponse response) {

        //Get the Logged in User
        org.springframework.security.core.userdetails.User user2 = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = user2.getUsername();

        RetrieveItems ri = new RetrieveItems();

        String type = request.getParameter("type");
        //Pass back all data from WOrk table


        if (type.equals("active"))
            return ri.getItemsDataSQL(name) ;
        else
            return ri.getArchiveData(name) ;
    }


    //This is invoked when we want to return a work item to modify
    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    @ResponseBody
    String modifyWork(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        RetrieveItems ri = new RetrieveItems();
        return ri.GetItemSQL(id) ;
    }

    //This is invoked when we retrieve all items for a given writer
    @RequestMapping(value = "/work", method = RequestMethod.POST)
    @ResponseBody
    String getWork(HttpServletRequest request, HttpServletResponse response) {

        InjectWorkService ws = new InjectWorkService();

        WorkItem item = new WorkItem();
        String description = request.getParameter("description");
        String date = request.getParameter("date");
        String guide = request.getParameter("guide");
        String status = request.getParameter("status");

        item.SetDate(date);
        item.SetName(getLoggedUser());
        item.SetDescription(description);
        item.SetGuide(guide);
        item.SetStatus(status);

        // Persist the data
        String itemNum =ws.injestNewSubmission(item);

        //Document xml = s3.toXml(allBuckets);
        //String bucketsStr= s3.convertToString(xml);
        return itemNum ;
    }

    private String getLoggedUser()
    {
        //Get the Logged in User
        org.springframework.security.core.userdetails.User user2 = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = user2.getUsername();
        return name;
    }

}
