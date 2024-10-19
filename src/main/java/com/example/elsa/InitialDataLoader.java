package com.example.elsa;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.example.elsa.domain.qna.entity.Standard;
import com.example.elsa.domain.qna.service.StandardService;

import lombok.RequiredArgsConstructor;

@Configuration
@Component
@RequiredArgsConstructor
public class InitialDataLoader {
	private final StandardService standardService;

	@Bean
	CommandLineRunner loadInitialData() {
		return args -> {
			List<Standard> initialStandards = List.of(
				new Standard("인권보장",
					"인공지능의 개발과 활용은 모든 인간에게 동등하게 부여된 권리를 존중하고, 다양한 민주적 가치와 국제인권법 등에 명시된 권리를 보장하여야 한다. 인공지능의 개발과 활용은 인간의 권리와 자유를 침해해서는 안 된다."),
				new Standard("프라이버시 보호",
					"인공지능을 개발하고 활용하는 전 과정에서 개인의 프라이버시를 보호해야 한다. 인공지능 전 생애주기에 걸쳐 개인 정보의 오용을 최소화 하도록 노력해야 한다."),
				new Standard("다양성 존중",
					"인공지능 개발 및 활용 전 단계에서 사용자의 다양성과 대표성을 반영해야 하며, 성별·연령·장애·지역·인종·종교·국가 등 개인 특성에 따른 편향과 차별을 최소화하고, 상용화된 인공지능은 모든 사람에게 공정하게 적용되어야 한다. 사회적 약자 및 취약 계층의 인공지능 기술 및 서비스에 대한 접근성을 보장하고, 인공지능이 주는 혜택은 특정 집단이 아닌 모든 사람에게 골고루 분배되도록 노력해야 한다."),
				new Standard("침해 금지",
					"인공지능을 인간에게 직·간접적인 해를 입히는 목적으로 활용해서는 안 된다. 인공지능이 야기할 수 있는 위험과 부정적 결과에 대응 방안을 마련하도록 노력해야 한다."),
				new Standard("공공성",
					"인공지능은 개인적 행복 추구뿐만 아니라 사회적 공공성 증진과 인류의 공동 이익을 위해 활용해야 한다. 인공지능은 긍정적 사회변화를 이끄는 방향으로 활용되어야 한다. 인공지능의 순기능을 극대화하고 역기능을 최소화하기 위한 교육을 다방면으로 시행하여야 한다."),
				new Standard("연대성",
					"다양한 집단 간의 관계 연대성을 유지하고, 미래세대를 충분히 배려하여 인공지능을 활용해야 한다. 인공지능 전 주기에 걸쳐 다양한 주체들의 공정한 참여 기회를 보장하여야 한다. 윤리적 인공지능의 개발 및 활용에 국제사회가 협력하도록 노력해야 한다."),
				new Standard("데이터 관리",
					"개인정보 등 각각의 데이터를 그 목적에 부합하도록 활용하고, 목적 외 용도로 활용하지 않아야 한다. 데이터 수집과 활용의 전 과정에서 데이터 편향성이 최소화되도록 데이터 품질과 위험을 관리해야 한다."),
				new Standard("책임성",
					"인공지능 개발 및 활용과정에서 책임 주체를 설정함으로써 발생할 수 있는 피해를 최소화하도록 노력해야 한다. 인공지능 설계 및 개발자, 서비스 제공자, 사용자 간의 책임소재를 명확히 해야 한다."),
				new Standard("안전성",
					"인공지능 개발 및 활용 전 과정에 걸쳐 잠재적 위험을 방지하고 안전을 보장할 수 있도록 노력해야 한다. 인공지능 활용 과정에서 명백한 오류 또는 침해가 발생할 때 사용자가 그 작동을 제어할 수 있는 기능을 갖추도록 노력해야 한다."),
				new Standard("투명성",
					"사회적 신뢰 형성을 위해 타 원칙과의 상충관계를 고려하여 인공지능 활용 상황에 적합한 수준의 투명성과 설명 가능성을 높이려는 노력을 기울여야 한다. 인공지능 기반 제품이나 서비스를 제공할 때 인공지능의 활용 내용과 활용 과정에서 발생할 수 있는 위험 등의 유의사항을 사전에 고지해야 한다.")
			);
			standardService.addInitialStandards(initialStandards);
		};
	}
}