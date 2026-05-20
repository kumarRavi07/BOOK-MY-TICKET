package com.jsp.book.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterDto {

	@NotBlank(message = "* Theater Name is Required")
	@Size(min = 3, max = 50, message = "* Enter between 3 ~ 50 characters")
	private String name;

	@NotBlank(message = "* Address is Required")
	@Size(min = 3, max = 200, message = "* Enter between 3 ~ 200 characters")
	private String address;

	@NotBlank(message = "* Location Link is Required")
	private String locationLink;

	private MultipartFile image;
}
