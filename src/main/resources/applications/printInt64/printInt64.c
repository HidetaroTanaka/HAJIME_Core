int main(int argc, char** argv) {
    volatile char string[19];
    int64ToHex(0x1145141919810, string);
    printstr(string);
    printstr("\n");
    return 0;
}