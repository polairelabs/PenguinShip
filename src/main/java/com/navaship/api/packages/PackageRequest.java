package com.navaship.api.packages;

import com.navaship.api.validators.BigDecimalLength;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PackageRequest {
    @NotNull
    @NotBlank
    @Size(max = 200)
    private String name;
    @NotNull
    @BigDecimalLength(maxLength = 6)
    @DecimalMin(value = "0.1", message = "Weight must be greater than zero")
    private BigDecimal weight;
    @BigDecimalLength(maxLength = 6)
    @DecimalMin(value = "0.01", message = "Value must be greater than zero")
    @Positive(message = "Value must be greater than 0")
    private BigDecimal value;
    @BigDecimalLength(maxLength = 6)
    @Positive(message = "Length must be greater than 0")
    private BigDecimal length;
    @BigDecimalLength(maxLength = 6)
    @Positive(message = "Width must be greater than 0")
    private BigDecimal width;
    @BigDecimalLength(maxLength = 6)
    @Positive(message = "Height must be greater than 0")
    private BigDecimal height;

    @AssertTrue(message = "Length, width and height are required, if one is present in the request")
    public boolean isValidRequest() {
        // if all three fields are sent with the request (valid request)
        if (length != null && width != null && height != null)
            return true;

        // if one is null, return false (invalid request), else return true if all of them are null (valid request)
        return length == null && width == null && height == null;
    }
}
