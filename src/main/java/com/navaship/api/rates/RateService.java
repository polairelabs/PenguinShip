package com.navaship.api.rates;

import com.easypost.model.Rate;
import com.easypost.model.Shipment;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RateService {
    private RateRepository rateRepository;
    private ModelMapper modelMapper;


    public NavaRate createRate(NavaRate rate) {
        return rateRepository.save(rate);
    }

    public NavaRate convertToNavaRate(Rate rate) {
        return modelMapper.map(rate, NavaRate.class);
    }
}
