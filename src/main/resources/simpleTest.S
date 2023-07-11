main:
lui s0, 0x00114 #                       :0x00114437
addi s0, s0, 0x514 # li s0, 0x114514    :0x51440413
lui s1, 0x00001 # li s1, 0x00001000     :0x000014B7
sw s0, 0(s1) #                          :0x0084A023
_EXIT:
j _EXIT #                               :0x0000006F