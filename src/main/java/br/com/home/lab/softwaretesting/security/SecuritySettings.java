package br.com.home.lab.softwaretesting.security;

import org.aeonbits.owner.Config;

@Config.Sources({
        "classpath:security.access.properties"
})
public interface SecuritySettings extends Config {

    @DefaultValue("user")
    String user();

    @DefaultValue("password")
    String password();
}
