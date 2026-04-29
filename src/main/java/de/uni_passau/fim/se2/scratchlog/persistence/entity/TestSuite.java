package de.uni_passau.fim.se2.scratchlog.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "test_suite")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TestSuite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", referencedColumnName = "id")
    private Experiment experiment;

    @Column(name = "filename")
    private String filename;

    @Column(name = "test_implementation")
    private String testImplementation;

    @OneToMany(
        mappedBy = "testSuite",
        orphanRemoval = true,
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER
    )
    private Set<TestCase> testCases;
}
