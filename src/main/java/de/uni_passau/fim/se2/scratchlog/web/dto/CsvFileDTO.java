package de.uni_passau.fim.se2.scratchlog.web.dto;

import de.uni_passau.fim.se2.scratchlog.util.validation.annotation.ValidFile;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CsvFileDTO {

    @ValidFile(contentTypes = {"text/csv"}, fileEndings = {"csv"})
    @NotNull
    private MultipartFile file;

}
