package com.bukeu.moment.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 2015/4/7.
 */
public class MomentList extends BaseModelList {

    private List<Moment> moments = new ArrayList<>();

    public MomentList() {
    }

    public MomentList(int page) {
        this.page = page;
    }

    public MomentList(int page, List<Moment> moments) {
        this.page = page;
        this.moments = moments;
    }

    public MomentList(int page, int size, int count, List<Moment> moments) {
        this.page = page;
        this.size = size;
        this.count = count;
        this.moments = moments;
    }

    public List<Moment> getMoments() {
        return moments;
    }

    public void setMoments(List<Moment> moments) {
        this.moments = moments;
    }
}
