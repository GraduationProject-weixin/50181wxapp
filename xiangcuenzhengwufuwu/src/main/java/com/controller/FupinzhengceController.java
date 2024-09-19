
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 扶贫政策
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/fupinzhengce")
public class FupinzhengceController {
    private static final Logger logger = LoggerFactory.getLogger(FupinzhengceController.class);

    @Autowired
    private FupinzhengceService fupinzhengceService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = fupinzhengceService.queryPage(params);

        //字典表数据转换
        List<FupinzhengceView> list =(List<FupinzhengceView>)page.getList();
        for(FupinzhengceView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        FupinzhengceEntity fupinzhengce = fupinzhengceService.selectById(id);
        if(fupinzhengce !=null){
            //entity转view
            FupinzhengceView view = new FupinzhengceView();
            BeanUtils.copyProperties( fupinzhengce , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody FupinzhengceEntity fupinzhengce, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,fupinzhengce:{}",this.getClass().getName(),fupinzhengce.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");

        Wrapper<FupinzhengceEntity> queryWrapper = new EntityWrapper<FupinzhengceEntity>()
            .eq("fupinzhengce_name", fupinzhengce.getFupinzhengceName())
            .eq("fupinzhengce_fuli", fupinzhengce.getFupinzhengceFuli())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        FupinzhengceEntity fupinzhengceEntity = fupinzhengceService.selectOne(queryWrapper);
        if(fupinzhengceEntity==null){
            fupinzhengce.setCreateTime(new Date());
            fupinzhengceService.insert(fupinzhengce);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody FupinzhengceEntity fupinzhengce, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,fupinzhengce:{}",this.getClass().getName(),fupinzhengce.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(StringUtil.isEmpty(role))
//            return R.error(511,"权限为空");
        //根据字段查询是否有相同数据
        Wrapper<FupinzhengceEntity> queryWrapper = new EntityWrapper<FupinzhengceEntity>()
            .notIn("id",fupinzhengce.getId())
            .andNew()
            .eq("fupinzhengce_name", fupinzhengce.getFupinzhengceName())
            .eq("fupinzhengce_fuli", fupinzhengce.getFupinzhengceFuli())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        FupinzhengceEntity fupinzhengceEntity = fupinzhengceService.selectOne(queryWrapper);
        if("".equals(fupinzhengce.getFupinzhengcePhoto()) || "null".equals(fupinzhengce.getFupinzhengcePhoto())){
                fupinzhengce.setFupinzhengcePhoto(null);
        }
        if(fupinzhengceEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      fupinzhengce.set
            //  }
            fupinzhengceService.updateById(fupinzhengce);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        fupinzhengceService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        try {
            List<FupinzhengceEntity> fupinzhengceList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            FupinzhengceEntity fupinzhengceEntity = new FupinzhengceEntity();
//                            fupinzhengceEntity.setFupinzhengceName(data.get(0));                    //标题 要改的
//                            fupinzhengceEntity.setFupinzhengcePhoto("");//照片
//                            fupinzhengceEntity.setFupinzhengceFuli(data.get(0));                    //福利 要改的
//                            fupinzhengceEntity.setFupinzhengceContent("");//照片
//                            fupinzhengceEntity.setCreateTime(date);//时间
                            fupinzhengceList.add(fupinzhengceEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        fupinzhengceService.insertBatch(fupinzhengceList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = fupinzhengceService.queryPage(params);

        //字典表数据转换
        List<FupinzhengceView> list =(List<FupinzhengceView>)page.getList();
        for(FupinzhengceView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        FupinzhengceEntity fupinzhengce = fupinzhengceService.selectById(id);
            if(fupinzhengce !=null){


                //entity转view
                FupinzhengceView view = new FupinzhengceView();
                BeanUtils.copyProperties( fupinzhengce , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody FupinzhengceEntity fupinzhengce, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,fupinzhengce:{}",this.getClass().getName(),fupinzhengce.toString());
        Wrapper<FupinzhengceEntity> queryWrapper = new EntityWrapper<FupinzhengceEntity>()
            .eq("fupinzhengce_name", fupinzhengce.getFupinzhengceName())
            .eq("fupinzhengce_fuli", fupinzhengce.getFupinzhengceFuli())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        FupinzhengceEntity fupinzhengceEntity = fupinzhengceService.selectOne(queryWrapper);
        if(fupinzhengceEntity==null){
            fupinzhengce.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      fupinzhengce.set
        //  }
        fupinzhengceService.insert(fupinzhengce);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


}
