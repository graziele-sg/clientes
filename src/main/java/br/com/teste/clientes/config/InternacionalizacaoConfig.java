package br.com.teste.clientes.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class InternacionalizacaoConfig {	
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("ISO-8859-1");
		messageSource.setDefaultLocale(getLocale());
		return messageSource;
	}
	
	@Bean
	public LocalValidatorFactoryBean validatorFactoryBean() {
		LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
		bean.setValidationMessageSource(messageSource());
		return bean;
	}
	
	@Value("${idioma.padrao}") //propriedade injetada de application.properties
	private String idioma;
	
	private Locale getLocale() {
		Locale locale;
		if (idioma.equals("EN")) {
			locale = Locale.UK;
		} else {
			locale = Locale.getDefault(); //Utiliza o padrão do sistema operacional	 
		}
		return locale;
	}
	
	public String getMessage(String code) {
		return messageSource().getMessage(code, null, null, getLocale());
	}
}
