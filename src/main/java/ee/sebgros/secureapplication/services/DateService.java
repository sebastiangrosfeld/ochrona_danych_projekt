package ee.sebgros.secureapplication.services;

import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class DateService {
	public Date getValidDate() {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		cal.add(Calendar.HOUR, 2);
		return cal.getTime();
	}
}
