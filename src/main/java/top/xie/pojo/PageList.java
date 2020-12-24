package top.xie.pojo;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

public class PageList<T> implements Serializable {
    //做分页要多少数据
    //当前页面
    private long currentPage;
    //总数量
    private long totalCount;
    //每一页有多少数量
    private long pageSize;

    //总页数 = 总的数量/每页数量
    private long totalPage;

    public long getCurrentPage() {
        return currentPage;
    }

    public PageList(){

    }

    public PageList(long currentPage, long totalCount, long pageSize) {
        this.currentPage = currentPage;
        this.totalCount = totalCount;
        this.pageSize = pageSize;

        this.totalPage = this.totalCount/this.pageSize;
        //计算总的页数，
        //是否第一页，是否最后一页
        //第一页为0，最后一页为总的页码
        //10，每一页有10 ==》1
        //100，每一页有10 ==》10
        this.isFrist = this.currentPage==1;
        this.isLast = this.currentPage == this.totalPage;
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
        //第一页为0，最后一页
        this.isFrist = this.currentPage ==1;
        this.isLast = this.currentPage==this.totalPage;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public boolean isFrist() {
        return isFrist;
    }

    public void setFrist(boolean frist) {
        isFrist = frist;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public List<T> getContents() {
        return contents;
    }

    public void setContents(List<T> contents) {
        this.contents = contents;
    }

    //是否第一页
    private boolean isFrist;
    //是否最后一页
    private boolean isLast;
    //数据
    private List<T> contents;


    public void parsePage(Page<T> all) {
        setContents(all.getContent());
        setFrist(all.isFirst());
        setLast(all.isLast());
        setCurrentPage(all.getNumber()+1);
        setTotalCount(all.getTotalElements());
        setTotalPage(all.getTotalPages());
        setPageSize(all.getSize());
    }
}
