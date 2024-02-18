package br.com.home.lab.softwaretesting.util;

import lombok.experimental.UtilityClass;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

@UtilityClass
public class DataGen {

    private final Faker faker = new Faker();

    public Date date(){
        var months31Days = List.of(1,3,5,7,8,10,12);
        int month = number(12);
        int day = month == 2 ? number(28) :
                months31Days.contains(month) ? number(31) : number(30);
        int year = number(1995, LocalDate.now().getYear());
        return new GregorianCalendar(year, month-1, day).getTime();
    }

    public Date date(int day, int mont, int year){
        return new GregorianCalendar(year, mont-1, day).getTime();
    }

    public String productName(){
        return faker.commerce().productName();
    }

    public int number(int min, int max){
        return getRandom().nextInt(min, max+1);
    }

    public double moneyValue(){
        return moneyValue(700);
    }

    public double moneyValue(double max){
        return getRandom().nextDouble(max);
    }

    public int number(int max){
        return getRandom().nextInt(0, max+1);
    }

    private Random getRandom(){
        return new Random();
    }
}
