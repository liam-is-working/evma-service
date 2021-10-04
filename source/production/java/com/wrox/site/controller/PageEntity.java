package com.wrox.site.controller;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

public class PageEntity<T> implements Serializable {
    public List<T> content;
    public int pageNum;
    public int totalPageNum;
    public int size;
    public PageEntity( Page<T> p ){
        content = p.getContent();
        pageNum = p.getNumber();
        totalPageNum = p.getTotalPages();
        size = p.getSize();
    }
}