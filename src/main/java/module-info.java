module JobPortal {
    requires spring.context;
    requires spring.data.commons;
    requires static lombok;
    requires com.fasterxml.jackson.annotation;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires spring.web;
    requires spring.tx;
    requires spring.data.jpa;
}