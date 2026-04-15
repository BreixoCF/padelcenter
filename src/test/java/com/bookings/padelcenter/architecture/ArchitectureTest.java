package com.bookings.padelcenter.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
		packages = "com.bookings.padelcenter",
		importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

	/**
	 * Domain layer must not depend on infrastructure, web, or Spring.
	 * It is pure Java: records, exceptions, and port interfaces only.
	 */
	@ArchTest
	static final ArchRule domain_doesNotDependOnInfrastructureOrSpring =
			noClasses()
					.that().resideInAPackage("..domain..")
					.should().dependOnClassesThat()
					.resideInAnyPackage(
							"..infrastructure..",
							"org.springframework.."
					)
					.because("the domain must stay framework-agnostic");

	/**
	 * Application layer must not depend on infrastructure adapters.
	 * It may use Spring stereotypes (@Service, @Transactional, @PreAuthorize)
	 * but must never import persistence or web adapter classes.
	 */
	@ArchTest
	static final ArchRule application_doesNotDependOnInfrastructure =
			noClasses()
					.that().resideInAPackage("..application..")
					.should().dependOnClassesThat()
					.resideInAPackage("..infrastructure..")
					.because("application use cases must only depend on domain ports");

	/**
	 * Web controllers must not bypass use cases and call repositories directly.
	 */
	@ArchTest
	static final ArchRule controllers_doNotAccessRepositoriesDirectly =
			noClasses()
					.that().resideInAPackage("..infrastructure.inbound.web..")
					.should().dependOnClassesThat()
					.resideInAPackage("..outbound.db..")
					.because("controllers must invoke use cases, not repositories");

	/**
	 * JPA @Entity classes must never appear in the inbound web layer.
	 * Controllers and DTOs must be decoupled from the persistence model.
	 */
	@ArchTest
	static final ArchRule webLayer_doesNotImportEntityClasses =
			noClasses()
					.that().resideInAPackage("..infrastructure.inbound..")
					.should().dependOnClassesThat()
					.areAnnotatedWith("jakarta.persistence.Entity")
					.because("the web layer must not couple to JPA entities");

}
