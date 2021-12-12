package com.nowcoder.mall.controller;

import com.nowcoder.mall.common.ResponseModel;
import com.nowcoder.mall.entity.Item;
import com.nowcoder.mall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(path = "/item")
@CrossOrigin(origins = "${nowcoder.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @RequestMapping(path = "/list",method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel getItemList(){
        List<Item> items = itemService.findItemOnPromotion();
        return new ResponseModel(items);
    }

    @RequestMapping(path = "/detail/{id}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseModel getItemDetail(@PathVariable("id") int id){
        Item item = itemService.findItemId(id);
        return new ResponseModel(item);
    }
}
