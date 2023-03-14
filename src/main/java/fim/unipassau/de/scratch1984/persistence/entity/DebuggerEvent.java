package fim.unipassau.de.scratch1984.persistence.entity;

import fim.unipassau.de.scratch1984.util.enums.DebuggerEventSpecific;
import fim.unipassau.de.scratch1984.util.enums.DebuggerEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

/**
 * An entity representing a debugger event that resulted from user interaction with the Scratch debugger, not including
 * interaction with questions.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DebuggerEvent implements Event {

    /**
     * The unique ID of the debugger event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the debugger event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} during which the debugger event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The datetime at which the debugger event occurred.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The type of debugger event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private DebuggerEventType eventType;

    /**
     * The specific event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    private DebuggerEventSpecific event;

    /**
     * The block or target id of the debugger event.
     */
    @Column(name = "block_target_id")
    private String blockOrTargetID;

    /**
     * The target name or block opcode of the debugger event.
     */
    @Column(name = "name_opcode")
    private String nameOrOpcode;

    /**
     * Only applicable to select sprite event, null for everything else.
     */
    @Column(name = "original")
    private Integer original;

    /**
     * The number of the block executions of the debugger event.
     */
    @Column(name = "execution")
    private Integer execution;

}
