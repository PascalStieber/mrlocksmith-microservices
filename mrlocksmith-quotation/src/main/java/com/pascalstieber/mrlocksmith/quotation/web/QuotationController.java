package com.pascalstieber.mrlocksmith.quotation.web;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.pascalstieber.mrlocksmith.quotation.clients.OrderClient;
import com.pascalstieber.mrlocksmith.quotation.clients.RegisterClient;
import com.pascalstieber.mrlocksmith.quotation.data.Item;
import com.pascalstieber.mrlocksmith.quotation.data.ItemRepository;
import com.pascalstieber.mrlocksmith.quotation.data.OrderEntity;
import com.pascalstieber.mrlocksmith.quotation.data.Quotation;
import com.pascalstieber.mrlocksmith.quotation.data.QuotationRepository;
import com.pascalstieber.mrlocksmith.quotation.logic.QuotationService;

@Controller
public class QuotationController {

	private static final String REDIRECT_ON_HOST = "redirect:http://192.168.99.100:8080/";
	private static final String HOST_URL = "http://192.168.99.100:8080/";

	private final Logger log = LoggerFactory.getLogger(QuotationController.class);

	private QuotationRepository quotationRepository;
	private RegisterClient registerClient;
	private OrderClient orderClient;
	private QuotationService quotationService;
	private ItemRepository itemRepository;

	@Autowired
	public QuotationController(QuotationRepository quotationRepository, RegisterClient registerClient,
			OrderClient orderClient, QuotationService quotationService, ItemRepository itemRepository) {
		this.quotationRepository = quotationRepository;
		this.registerClient = registerClient;
		this.orderClient = orderClient;
		this.quotationService = quotationService;
		this.itemRepository = itemRepository;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView index() {
		List<OrderEntity> allOrders = orderClient.getAllOrders();
		return new ModelAndView("showAllOrders", "orders", allOrders);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ModelAndView indexWithContractor(@PathVariable("id") long contractorid,
			HttpServletRequest httpServletRequest) {
		List<OrderEntity> allOrders = orderClient.getAllOrders();
		httpServletRequest.getSession().setAttribute("contractorid", contractorid);
		return new ModelAndView("showAllOrders", "orders", allOrders);
	}

	@RequestMapping(value = "/formQuotation.html", method = RequestMethod.POST, params = "action=submitTender")
	public ModelAndView submitTender(HttpServletRequest httpServletRequest) {
		long orderid = Long.parseLong(httpServletRequest.getParameter("orderid"));
		long contractorid = Long.parseLong(httpServletRequest.getParameter("contractorid"));
		httpServletRequest.getSession().setAttribute("contractorid", contractorid);
		Quotation quotation = new Quotation();
		quotation.setOrderid(orderid);
		quotation.addItem(new Item());
		return new ModelAndView("submitTender", "quotation", quotation);
	}

	@RequestMapping(value = "/formQuotation.html", method = RequestMethod.POST, params = "action=editTender")
	public ModelAndView editTender(HttpServletRequest httpServletRequest) {
		long orderid = Long.parseLong(httpServletRequest.getParameter("orderid"));
		long contractorid = Long.parseLong(httpServletRequest.getParameter("contractorid"));
		httpServletRequest.getSession().setAttribute("contractorid", contractorid);
		Quotation quotation = quotationRepository.findByOrderidAndContractorid(orderid, contractorid).get(0);
		return new ModelAndView("submitTender", "quotation", quotation);
	}

	@RequestMapping(value = "/formQuotation.html", method = RequestMethod.POST, params = "action=addItem")
	public ModelAndView addItem(Quotation quotation, BindingResult bindingResult,
			HttpServletRequest httpServletRequest) {
		quotation.addItem(new Item());
		long contractorid = Long.parseLong(httpServletRequest.getParameter("contractorid"));
		httpServletRequest.getSession().setAttribute("contractorid", contractorid);
		return new ModelAndView("submitTender", "quotation", quotation);
	}

	@RequestMapping(value = "/removeItem.html", method = RequestMethod.POST)
	public ModelAndView removeItem(Quotation quotation, BindingResult bindingResult,
			HttpServletRequest httpServletRequest) {
		if (quotation.getId() != 0) {
			long contractorid = Long.parseLong(httpServletRequest.getParameter("contractorid"));
			httpServletRequest.getSession().setAttribute("contractorid", contractorid);
			long removeItemId = Long.parseLong(httpServletRequest.getParameter("removeItemId"));
			itemRepository.delete(removeItemId);
			quotation.setItems(itemRepository.findByQuotation(quotation));
		}
		return new ModelAndView("submitTender", "quotation", quotation);
	}

	@RequestMapping(value = "/formQuotation.html", method = RequestMethod.POST, params = "action=saveTender")
	public ModelAndView saveTender(@Valid Quotation quotation, BindingResult bindingResult,
			HttpServletRequest httpServletRequest) {
		if (!bindingResult.getAllErrors().isEmpty()) {
			return new ModelAndView("submitTender", "quotation", quotation);
		}
		long contractorid = Long.parseLong(httpServletRequest.getParameter("contractorid"));
		quotation.setContractorid(contractorid);
		quotation = quotationRepository.save(quotation);

		return new ModelAndView(REDIRECT_ON_HOST + "quotation/" + contractorid);
	}

	@RequestMapping(value = "/formQuotation.html", method = RequestMethod.POST, params = "action=cancelTender")
	public ModelAndView cancelTender(HttpServletRequest httpServletRequest) {
		long orderid = Long.parseLong(httpServletRequest.getParameter("orderid"));
		long contractorid = Long.parseLong(httpServletRequest.getParameter("contractorid"));
		List<Quotation> quotation = quotationRepository.findByOrderidAndContractorid(orderid, contractorid);
		quotationRepository.delete(quotation.get(0));
		return new ModelAndView(REDIRECT_ON_HOST + "quotation/"+contractorid);
	}

	// ========================= ENDE
	@RequestMapping(value = "/findAllQuotationsForOrder/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<Quotation> findAllQuotationsForOrder(@PathVariable("id") long id) {
		List<Quotation> quotation = quotationRepository.getAllQuotationsForOrder(id);
		return quotation;
	}

	@RequestMapping(value = "/getSumOfQuotation/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String getSumOfQuotation(@PathVariable("id") long id) {
		String sum = itemRepository.getSumOfQuotation(quotationRepository.findOne(id));
		return sum;
	}

	@RequestMapping(value = "/findAllItemsByQuotationId/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<Item> findAllItemsByQuotationId(@PathVariable("id") long id) {
		List<Item> items = itemRepository.findByQuotation(quotationRepository.findOne(id));
		return items;
	}

	@RequestMapping(value = "/acceptQuotation/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Boolean acceptQuotation(@PathVariable("id") long id, HttpServletRequest httpServletRequest) {
		Quotation quotation = quotationRepository.findOne(id);
		quotation.setAccepted(true);
		quotation.setAcceptedAt(new Date());
		quotationRepository.save(quotation);
		return true;
	}
}
