package com.example.elsa.domain.member.entity;

public enum Country {
	KOREA("한국"),
	USA("미국"),
	JAPAN("일본"),
	CHINA("중국"),
	FRANCE("프랑스"),
	GERMANY("독일"),
	ITALY("이탈리아"),
	UK("영국"),
	CANADA("캐나다"),
	AUSTRALIA("호주"),
	NEW_ZEALAND("뉴질랜드"),
	NETHERLANDS("네덜란드"),
	SWEDEN("스웨덴"),
	NORWAY("노르웨이"),
	DENMARK("덴마크"),
	FINLAND("핀란드"),
	SWITZERLAND("스위스"),
	AUSTRIA("오스트리아"),
	BELGIUM("벨기에"),
	SPAIN("스페인"),
	PORTUGAL("포르투갈"),
	GREECE("그리스"),
	TURKEY("터키"),
	POLAND("폴란드"),
	CZECH_REPUBLIC("체코"),
	SLOVAKIA("슬로바키아"),
	HUNGARY("헝가리"),
	ROMANIA("루마니아"),
	BULGARIA("불가리아"),
	CROATIA("크로아티아"),
	SERBIA("세르비아"),
	SLOVENIA("슬로베니아"),
	BOSNIA_AND_HERZEGOVINA("보스니아 헤르체고비나"),
	MONTENEGRO("몬테네그로"),
	MACEDONIA("북마케도니아"),
	ALBANIA("알바니아"),
	KOSOVO("코소보"),
	UKRAINE("우크라이나"),
	BELARUS("벨라루스"),
	RUSSIA("러시아"),
	KAZAKHSTAN("카자흐스탄"),
	UZBEKISTAN("우즈베키스탄"),
	TURKMENISTAN("투르크메니스탄"),
	TAJIKISTAN("타지키스탄"),
	KYRGYZSTAN("키르기스스탄"),
	GEORGIA("조지아"),
	ARMENIA("아르메니아"),
	AZERBAIJAN("아제르바이잔"),
	MOLDOVA("몰도바"),
	LITHUANIA("리투아니아"),
	LATVIA("라트비아"),
	ESTONIA("에스토니아"),
	ICELAND("아이슬란드"),
	IRELAND("아일랜드"),
	LUXEMBOURG("룩셈부르크"),
	MALTA("몰타"),
	CYPRUS("키프로스");

	private final String description;

	Country(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public static Country fromDescription(String description) {
		for (Country country : Country.values()) {
			if (country.getDescription().equals(description)) {
				return country;
			}
		}
		throw new IllegalArgumentException("No matching country for description: " + description);
	}
}
