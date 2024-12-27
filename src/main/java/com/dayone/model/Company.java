package com.dayone.model;

import lombok.Builder;
import lombok.Data;

/*
CompanyEntity 클래스를 사용하지 않고 model 클래스를 (Company) 따로 정의해준 이유?
	- entity 는 DB 와 직접적으로 매핑되기 위한 클래스이기 때문에,
	entity 인스턴스를 서비스 내부 코드에서 데이터를 주고 받기 위한 용도로 쓰거나
	이 과정에서 데이터 내용을 변경하는 로직이 들어가게 되면 클래스의 원래 역할 범위를 벗어나게 됨
 */
@Data
@Builder
public class Company {
	private String ticker;
	private String name;
}
