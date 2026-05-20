package com.jsp.book.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.jsp.book.dto.UserDto;
import com.jsp.book.entity.BookedTicket;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

	private static final String USER_DTO_KEY = "dto-";
	private static final String OTP_KEY = "otp-";
	private static final java.util.Map<String, Object> storage = new java.util.concurrent.ConcurrentHashMap<>();

	// In-memory implementation requires no RedisTemplate locally

	@Override
	@Async
	public void saveUserDto(String email, UserDto userDto) {
		storage.put(USER_DTO_KEY + email, userDto);
	}

	@Override
	@Async
	public void saveOtp(String email, int otp) {
		storage.put(OTP_KEY + email, otp);
	}

	@Override
	public UserDto getUserDto(String email) {
		Object value = storage.get(USER_DTO_KEY + email);
		return (value instanceof UserDto dto) ? dto : null;
	}

	@Override
	public int getOtp(String email) {
		Object value = storage.get(OTP_KEY + email);
		return (value instanceof Integer otp) ? otp : 0;
	}

	@Override
	@Async
	public void saveTicket(String orderId, BookedTicket ticket) {
		storage.put(orderId, ticket);
	}

	@Override
	public BookedTicket getTicket(String orderId) {
		Object value = storage.get(orderId);
		return (value instanceof BookedTicket ticket) ? ticket : null;
	}
}
