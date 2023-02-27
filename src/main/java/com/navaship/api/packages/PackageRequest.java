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
    @Digits(integer = 6, fraction = 1, message = "Only one decimal point is allowed")
    @DecimalMin(value = "0.1", message = "Weight must be greater than zero")
    private BigDecimal weight;
    @Digits(integer = 6, fraction = 2, message = "Only two decimal points are allowed")
    @DecimalMin(value = "0.01", message = "Value must be greater than zero")
    private BigDecimal value;
    @Digits(integer = 6, fraction = 1, message = "Only one decimal point is allowed")
    @DecimalMin(value = "0.1", message = "Length must be greater than zero")
    private BigDecimal length;
    @Digits(integer = 6, fraction = 1, message = "Only one decimal point is allowed")
    @DecimalMin(value = "0.1", message = "Width must be greater than zero")
    private BigDecimal width;
    @Digits(integer = 6, fraction = 1, message = "Only one decimal point is allowed")
    @DecimalMin(value = "0.1", message = "Height must be greater than zero")
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
