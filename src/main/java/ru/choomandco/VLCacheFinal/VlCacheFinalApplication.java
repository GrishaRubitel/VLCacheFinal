package ru.choomandco.VLCacheFinal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в приложение. Фукнкция запускает процесс подняетия всех созданных сервис и подгрузки зависимостей к ним (ОРМ, Кафка конфиги)
 */
@SpringBootApplication
public class VlCacheFinalApplication {

	public static void main(String[] args) {
		SpringApplication.run(VlCacheFinalApplication.class, args);
		System.out.println("start");
	}

}
