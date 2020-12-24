package top.xie.services.Impl;

import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xie.dao.ArticleDao;
import top.xie.pojo.Article;
import top.xie.utils.Constants;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class SolrTestService {

    @Autowired
    private SolrClient solrClient;

    public void add(){
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id","782540422273564672");
        doc.addField("blog_view_count",10);
        doc.addField("blog_category_id","10");

        doc.addField("blog_title","qw ");
        doc.addField("blog_content","<p>this is content</p> ");
        doc.addField("blog_create_time",new Date());
        doc.addField("blog_labels","测试-博客");
        doc.addField("blog_url","http://baidu.com");

        try {
            solrClient.add(doc);
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void update(){
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id","782540422273564672");
        doc.addField("blog_view_count",10);
        doc.addField("blog_category_id","10");

        doc.addField("blog_title","qw ");
        doc.addField("blog_content","<p>this is content</p> ");
        doc.addField("blog_create_time",new Date());
        doc.addField("blog_labels","测试-博客");
        doc.addField("blog_url","http://baidu.com");

        try {
            solrClient.add(doc);
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void delete(){
        try {
            //单独删除一条记录
            solrClient.deleteById("782540422273564672");

            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAll(){
        try {

            //删除所以
            solrClient.deleteByQuery("*");
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private ArticleDao articleDao;

    public void importAll(){
        List<Article> all = articleDao.findAll();
        for (Article article : all) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id",article.getId());
            doc.addField("blog_view_count",article.getViewCount());
            doc.addField("blog_category_id",article.getCategoryId());
            doc.addField("blog_title",article.getTitle());
            //对内容进行处理，去掉标签，提取纯文本
            //第一种是markdown 写的内容 ---> type =1
            //第二种是富文本 ===> type = 0
            //如果type ===1 ===> 转html
            //再由html ===> 纯文本
            // 如果type==0 ==> 纯文本
            String type = article.getType();
            String html;
            if (Constants.Article.TYPE_MARKDOWN.equals(type)) {
                //转html
                // markdown to html
                MutableDataSet options = new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create(),
                        JekyllTagExtension.create(),
                        TocExtension.create(),
                        SimTocExtension.create()
                ));
                Parser parser = Parser.builder(options).build();
                HtmlRenderer renderer = HtmlRenderer.builder(options).build();
                Node document = parser.parse(article.getContent());

                html = renderer.render(document);
            }else {
                html = article.getContent();
            }
            //到这里，不管原来是什么，现在都是Html
            //html == > 富文本 text
            String content = Jsoup.parse(html).text();

            doc.addField("blog_content",content);
            doc.addField("blog_create_time",new Date());
            doc.addField("blog_labels",article.getLabel());
            doc.addField("blog_url","http://baidu.com");
            try {
                solrClient.add(doc);
                solrClient.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
