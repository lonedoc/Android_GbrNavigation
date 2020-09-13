package gbr.utils.callbacks

import gbr.utils.adapters.login.AdapterIpAddresses

interface ValidateAddress {
    fun validateAddress(
        holder: AdapterIpAddresses.ViewHolder,
        address: String
    )
}