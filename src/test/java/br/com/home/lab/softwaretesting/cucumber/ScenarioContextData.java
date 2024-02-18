package br.com.home.lab.softwaretesting.cucumber;

import org.testng.Assert;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContextData {

    private Map<String, Object> context;

    public ScenarioContextData(){
        context = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> void setContext(String key, T value){
        context.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key){
        T value = (T)context.get(key);
        Assert.assertNotNull(value, "O valor a ser recuperado nao pode ser nulo");
        return value;
    }
}
