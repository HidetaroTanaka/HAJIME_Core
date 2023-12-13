set_property PACKAGE_PIN E3 [get_ports clk_in1_0]
set_property IOSTANDARD LVCMOS33 [get_ports clk_in1_0]
create_clock -period 10.0 [get_ports clk_in1_0]

set_property IOSTANDARD LVCMOS33 [get_ports RST_0]
set_property PACKAGE_PIN C12 [get_ports RST_0]

# number of 7seg
set_property IOSTANDARD LVCMOS33 [get_ports {SEG_0[*]}]
set_property PACKAGE_PIN L3 [get_ports {SEG_0[0]}]
set_property PACKAGE_PIN N1 [get_ports {SEG_0[1]}]
set_property PACKAGE_PIN L5 [get_ports {SEG_0[2]}]
set_property PACKAGE_PIN L4 [get_ports {SEG_0[3]}]
set_property PACKAGE_PIN K3 [get_ports {SEG_0[4]}]
set_property PACKAGE_PIN M2 [get_ports {SEG_0[5]}]
set_property PACKAGE_PIN L6 [get_ports {SEG_0[6]}]

# location of 7seg
set_property IOSTANDARD LVCMOS33 [get_ports {AN_0[*]}]
set_property PACKAGE_PIN N6 [get_ports {AN_0[0]}]
set_property PACKAGE_PIN M6 [get_ports {AN_0[1]}]
set_property PACKAGE_PIN M3 [get_ports {AN_0[2]}]
set_property PACKAGE_PIN N5 [get_ports {AN_0[3]}]
set_property PACKAGE_PIN N2 [get_ports {AN_0[4]}]
set_property PACKAGE_PIN N4 [get_ports {AN_0[5]}]
set_property PACKAGE_PIN L1 [get_ports {AN_0[6]}]
set_property PACKAGE_PIN M1 [get_ports {AN_0[7]}]

