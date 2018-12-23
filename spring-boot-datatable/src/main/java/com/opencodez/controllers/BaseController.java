/**
 * 
 */
package com.opencodez.controllers;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.google.gson.Gson;
import com.opencodez.domain.User;
import com.opencodez.domain.UserModel;
import com.opencodez.domain.pagination.DataTableRequest;
import com.opencodez.domain.pagination.DataTableResults;
import com.opencodez.domain.pagination.PaginationCriteria;
import com.opencodez.repo.GenericRepo;
import com.opencodez.repo.GenericPagingRepository;
import com.opencodez.repo.UserRepository;
import com.opencodez.util.AppUtil;

/**
 * @author pavan.solapure
 *
 */
@Controller
public class BaseController {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private GenericRepo genericRepo;
	
	@Autowired
	private GenericPagingRepository pagingRepo;
	
	/** The entity manager. */
	@PersistenceContext
	private EntityManager entityManager;
	
		
	@RequestMapping(value="/", method = RequestMethod.GET)
	public ModelAndView home(@RequestParam(value = "name", defaultValue = "World") String name) {
		ModelAndView mv = new ModelAndView("index");
		mv.addObject("userModel", new UserModel());
		List<UserModel> userList  = genericRepo.getUserModel();
		mv.addObject("userlist", userList);
		return mv;
	}

	@RequestMapping(value="/users", method=RequestMethod.GET)
	public String listUsersOracle(Model model) {
		return "users";
	}
	
	@RequestMapping(value="/users/paginated/orcl", method=RequestMethod.GET)
	@ResponseBody
	public String listUsersPaginatedForOracle(HttpServletRequest request, HttpServletResponse response, Model model) {
		DataTableRequest<User> dataTableInRQ = new DataTableRequest<User>(request);
		String sortBy = dataTableInRQ.getOrder().getData();				
		Direction dir = Direction.fromString(dataTableInRQ.getOrder().getSortDir());		
		Sort sort = new Sort(new Sort.Order(dir, sortBy));
		
		Integer pageNo =(Integer.parseInt(request.getParameter(PaginationCriteria.PAGE_NO)));
		Integer pageSize = (Integer.parseInt(request.getParameter(PaginationCriteria.PAGE_SIZE)));		
		Pageable pageable = new PageRequest(pageNo / pageSize, pageSize, sort);				
        
		Page<User> page = pagingRepo.findAll(pageable);
        List<User> userListOld = page.getContent();
        
        List<UserModel> userList = new ArrayList<UserModel>();
        for(User user : userListOld) {
        	UserModel temp = new UserModel();
        	temp.setId(user.getId());
        	temp.setName(user.getName());
        	temp.setSalary(user.getSalary());
        	temp.setTotalRecords(20);
        	userList.add(temp);
        }
        
		DataTableResults<UserModel> dataTableResult = new DataTableResults<UserModel>();
		
		dataTableResult.setDraw(dataTableInRQ.getDraw());
		dataTableResult.setListOfDataObjects(userList);
		if (!AppUtil.isObjectEmpty(userList)) {
			dataTableResult.setRecordsTotal(userList.get(0).getTotalRecords()
					.toString());
			if (dataTableInRQ.getPaginationRequest().isFilterByEmpty()) {
				dataTableResult.setRecordsFiltered(userList.get(0).getTotalRecords()
						.toString());
			} else {
				dataTableResult.setRecordsFiltered(Integer.toString(userList.size()));
			}
		}
		return new Gson().toJson(dataTableResult);
	}
	
	@RequestMapping(value="/adduser", method=RequestMethod.POST)
	public String addUser(@ModelAttribute UserModel userModel, Model model) {
		if(null != userModel) {
			
			if(!AppUtil.isObjectEmpty(userModel.getId()) && 
					!AppUtil.isObjectEmpty(userModel.getName()) && 
					!AppUtil.isObjectEmpty(userModel.getSalary())) {
				
				User u = new User();
				u.setId(userModel.getId());
				u.setName(userModel.getName());
				u.setSalary(userModel.getSalary());
				userRepo.save(u);
			}
		}
		return "redirect:/";
	}

}
