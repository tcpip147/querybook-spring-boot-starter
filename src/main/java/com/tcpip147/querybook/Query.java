package com.tcpip147.querybook;

public class Query {

    private String id;
    private String name;
    private String creator;
    private String createdDate;
    private String sql;

    public Query(String id, String name, String creator, String createdDate, String sql, boolean injectComment) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.createdDate = createdDate;
        if (injectComment) {
            StringBuilder sb = new StringBuilder();
            sb.append("/*" + "\n");
            sb.append(" * ID: " + id + "\n");
            sb.append(" * 설명: " + name + "\n");
            sb.append(" * 작성자: " + creator + "\n");
            sb.append(" * 작성일자: " + createdDate + "\n");
            sb.append(" */" + "\n");
            sb.append(sql);
            this.sql = sb.toString();
        } else {
            this.sql = sql;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getSql() {
        return sql;
    }
}
