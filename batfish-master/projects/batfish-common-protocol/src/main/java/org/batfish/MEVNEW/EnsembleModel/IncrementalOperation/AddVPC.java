package org.batfish.MEVNEW.EnsembleModel.IncrementalOperation;

import java.util.List;

public class AddVPC {
    private List<AddCustomer> _addCustomers;
    public AddVPC(List<AddCustomer> addCustomers) {
        _addCustomers = addCustomers;
    }
    public List<AddCustomer> getAddCustomers() {
        return _addCustomers;
    }
    public void setAddCustomers(List<AddCustomer> addCustomers) {
        _addCustomers = addCustomers;
    }
}
