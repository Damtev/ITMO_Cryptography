//
// Created by damtev on 02.10.2019.
//

#include "des.h"

void Des::initial_perm(const bitset<64> &block, bitset<32> &left, bitset<32> &right) {
    for (size_t i = 0; i < right.size(); ++i)
        right[i] = block[ip_table[i] - 1];
    for (size_t i = 0; i < left.size(); ++i)
        left[i] = block[ip_table[i + left.size()] - 1];
}

void Des::to_s_and_p_blocks(bitset<32> &left, bitset<32> &right, const bitset<48> &sub_key) {
    bitset<48> to_short;
    bitset<32> to_perm;
    for (size_t i = 0; i < to_short.size(); ++i) {
        to_short[i] = right[extend_perm[i] - 1];
    }
    to_short ^= sub_key;
    std::bitset<4> col;
    std::bitset<2> row;
    for (size_t i = 0; i < 8; ++i) {
        row[0] = to_short[6 * i];
        row[1] = to_short[6 * i + 5];
        col[0] = to_short[6 * i + 1];
        col[1] = to_short[6 * i + 2];
        col[2] = to_short[6 * i + 3];
        col[4] = to_short[6 * i + 4];
        std::bitset<4> temp(s_box[i][row.to_ulong() * 16 + col.to_ulong()]);
        for (size_t j = 0; j < temp.size(); ++j)
            to_short[4 * i + j] = temp[j];
    }
    for (size_t i = 0; i < to_perm.size(); ++i)
        to_perm[i] = to_short[p_table[i] - 1];
    left ^= to_perm;
}

void Des::swap(bitset<32> &left, bitset<32> &right) {
    bitset<32> temp;
    for (size_t i = 0; i < temp.size(); ++i)
        temp[i] = left[i];
    for (size_t i = 0; i < left.size(); ++i)
        left[i] = right[i];
    for (size_t i = 0; i < right.size(); ++i)
        right[i] = temp[i];
}

void Des::inverse(const bitset<32> &left, const bitset<32> &right, bitset<64> &block) {
    for (size_t i = 0; i < block.size(); ++i) {
        if (inverse_ip_table[i] <= 32)
            block[i] = right[inverse_ip_table[i] - 1];
        else
            block[i] = left[inverse_ip_table[i] - 32 - 1];
    }
}

bitset<48> Des::get_sub_key(unsigned int n, const bitset<64> &bkey) {
    bitset<48> result;
    bitset<56> key;
    unsigned int klen = key.size(), rlen = result.size();
    for (size_t i = 0; i < key.size(); ++i)
        key[i] = bkey[key_table[i] - 1];
    for (size_t i = 0; i <= n; ++i) {
        for (size_t j = 0; j < bit_shift[i]; ++j) {
            result[rlen - bit_shift[i] + j] = key[klen - bit_shift[i] + j];
            result[rlen / 2 - bit_shift[i] + j] = key[klen / 2 - bit_shift[i] + j];
        }
        key <<= bit_shift[i];
        for (size_t j = 0; j < bit_shift[i]; ++j) {
            key[klen / 2 + j] = result[rlen - bit_shift[i] + j];
            key[j] = result[rlen / 2 - bit_shift[i] + j];
        }
    }
    for (size_t i = 0; i < result.size(); ++i)
        result[i] = key[key_perm[i] - 1];
    return result;
}

void Des::crypt(bitset<64> &block, bitset<64> &key, const unsigned long option) {
    bitset<32> left, right;
    initial_perm(block, left, right);
    switch (option) {
        case encrypt:
            for (char i = 0; i < 16; ++i) {
                bitset<48> sub_key = get_sub_key(i, key);
                to_s_and_p_blocks(left, right, sub_key);
                if (i != 15) {
                    swap(left, right);
                }
            }
            break;
        case decrypt:
            for (char i = 15; i >= 0; --i) {
                bitset<48> sub_key = get_sub_key(i, key);
                to_s_and_p_blocks(left, right, sub_key);
                if (i != 0) {
                    swap(left, right);
                }
            }
            break;
        default:
            break;
    }
    inverse(left, right, block);
}
