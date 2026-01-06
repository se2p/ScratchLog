package de.uni_passau.fim.se2.scratchlog;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public abstract class AbstractScratchLogControllerTest extends AbstractScratchLogTest {

    @Autowired
    protected MockMvc mvc;

}
