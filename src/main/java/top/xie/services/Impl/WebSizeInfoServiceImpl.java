package top.xie.services.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xie.dao.SettingDao;
import top.xie.pojo.Setting;
import top.xie.response.ResponseResult;
import top.xie.services.IWebInfoService;
import top.xie.utils.Constants;
import top.xie.utils.IdWorker;
import top.xie.utils.RedisUtil;
import top.xie.utils.TextUtils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class WebSizeInfoServiceImpl extends BaseService implements IWebInfoService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SettingDao settingDao;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public ResponseResult getWebSizeTitle() {
        Setting title = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_TITLE);
        return ResponseResult.SUCCESS("获取网站title成功").setData(title);
    }

    @Override
    public ResponseResult putWebSizeTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return ResponseResult.FAILED("网站标题不可以为空.");
        }
        Setting titleFromDb = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_TITLE);
        if (titleFromDb==null) {
            titleFromDb = new Setting();
            titleFromDb.setId(idWorker.nextId()+"");
            titleFromDb.setUpdateTime(new Date());
            titleFromDb.setCreateTime(new Date());
            titleFromDb.setKey(Constants.Settings.WEB_SIZE_TITLE);
        }
        titleFromDb.setValue(title);
        settingDao.save(titleFromDb);
        return ResponseResult.SUCCESS("网站Title更新成功.");
    }

    @Override
    public ResponseResult getSeoInfo() {
        Setting description = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_DESCRIPTION);
        Setting keyWords = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_KEYWORDS);
        Map<String,String> result = new HashMap<>();
        if (description != null && keyWords != null) {
            result.put(description.getKey(), description.getValue());
            result.put(keyWords.getKey(), keyWords.getValue());
        }

        return ResponseResult.SUCCESS("获取SEO信息成功.").setData(result);
    }

    @Override
    public ResponseResult putSeoInfo(String keyWords, String description) {
        //判断
        if (TextUtils.isEmpty(keyWords)) {
            return ResponseResult.FAILED("关键字不可以为空.");
        }
        if (TextUtils.isEmpty(description)) {
            return ResponseResult.FAILED("网站描述不可以为空.");
        }
        Setting descriptionFromDb = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_DESCRIPTION);
        if (descriptionFromDb == null) {
            descriptionFromDb = new Setting();
            descriptionFromDb.setId(idWorker.nextId() + "");
            descriptionFromDb.setCreateTime(new Date());
            descriptionFromDb.setUpdateTime(new Date());
            descriptionFromDb.setKey(Constants.Settings.WEB_SIZE_DESCRIPTION);
        }
        descriptionFromDb.setValue(description);
        settingDao.save(descriptionFromDb);
        Setting keyWordsFromDb = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_KEYWORDS);
        if (keyWordsFromDb == null) {
            keyWordsFromDb = new Setting();
            keyWordsFromDb.setId(idWorker.nextId() + "");
            keyWordsFromDb.setCreateTime(new Date());
            keyWordsFromDb.setUpdateTime(new Date());
            keyWordsFromDb.setKey(Constants.Settings.WEB_SIZE_KEYWORDS);
        }
        keyWordsFromDb.setValue(keyWords);
        settingDao.save(keyWordsFromDb);
        return ResponseResult.SUCCESS("更新SEO信息成功.");
    }
    /**
     * 这个是全网站的访问量，要做得细一点，还得分来源
     * 这里只统计浏览量，只统计文章的浏览量，提供一个浏览量的统计接口（页面级的）
     *
     * @return 浏览量
     */
    @Override
    public ResponseResult getWebSizeViewCount() {
        //先从redis里拿出来
        String viewCountStr = (String) redisUtil.get(Constants.Settings.WEB_SIZE_VIEW_COUNT);
        Setting viewCountFromDb = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_VIEW_COUNT);
        if (viewCountFromDb == null) {
            viewCountFromDb = this.initViewItem();
            settingDao.save(viewCountFromDb);
        }
        if (TextUtils.isEmpty(viewCountStr)) {
            viewCountStr = viewCountFromDb.getValue();
            redisUtil.set(Constants.Settings.WEB_SIZE_VIEW_COUNT,viewCountStr);
        }else{
            //把redis里的更新到数据里
            viewCountFromDb.setValue(viewCountStr);
            settingDao.save(viewCountFromDb);
        }

        Map<String, Integer> result = new HashMap<>();
        result.put(viewCountFromDb.getKey(), Integer.valueOf(viewCountFromDb.getValue()));
        return ResponseResult.SUCCESS("获取文章浏览量成功.").setData(result);
    }



    private Setting initViewItem(){
        Setting viewCount = new Setting();
        viewCount.setId(idWorker.nextId() + "");
        viewCount.setCreateTime(new Date());
        viewCount.setUpdateTime(new Date());
        viewCount.setKey(Constants.Settings.WEB_SIZE_VIEW_COUNT);
        viewCount.setValue("1");
        return viewCount;
    }
    /**
     * 1、并发量
     * 2、过滤相通的IP/ID
     * 3、防止攻击，比如太频繁的访问，就提示请稍后重试.
     */
    @Override
    public void updateViewCount() {
        //redis的更新时机：
        Object viewCount = redisUtil.get(Constants.Settings.WEB_SIZE_VIEW_COUNT);
        if (viewCount == null) {
            Setting setting = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_VIEW_COUNT);
            if (setting == null) {
                setting = this.initViewItem();
                settingDao.save(setting);
            }
            redisUtil.set(Constants.Settings.WEB_SIZE_VIEW_COUNT, setting.getValue());
        } else {
            //自增
            redisUtil.incr(Constants.Settings.WEB_SIZE_VIEW_COUNT, 1);
        }
    }
}
