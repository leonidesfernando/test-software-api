package br.com.home.lab.softwaretesting.controller.record;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record FormSearch(String searchItem, boolean searchOnlyCurrentMonth, int page){}
