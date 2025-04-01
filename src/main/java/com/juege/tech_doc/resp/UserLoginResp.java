package com.juege.tech_doc.resp;

import com.juege.tech_doc.domain.User;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = User.class)
public class UserLoginResp {
	private Long id;

	private String loginName;

	private String name;

	private String token;

	private Long points;

}
