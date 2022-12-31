package com.navaship.api.subscriptiondetail;

import com.navaship.api.appuser.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SubscriptionDetailService {
    private SubscriptionDetailRepository subscriptionDetailRepository;


    public SubscriptionDetail createSubscriptionDetail(SubscriptionDetail subscriptionDetail) {
        return subscriptionDetailRepository.save(subscriptionDetail);
    }

    public void deleteSubscriptionDetail(AppUser user) {
        subscriptionDetailRepository.deleteById(user.getId());
    }
}
