package com.bukeu.moment.model;

/**
 * Created by Max on 2015/4/16.
 *
 * For pageable response.
 *
 */
public class BaseModelList extends BaseModel {

    public static final int DEFAULT_SIZE = 5;

    protected int page;
    protected int size;
    protected int count;

    public BaseModelList() {

    }

    public BaseModelList(int page) {
        this.page = page;
    }

    public BaseModelList(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
