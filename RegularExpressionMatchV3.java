package com.ysm.offer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
/*
    通过leetcode测试
 */

public class RegularExpressionMatchV3 {


    private static Object Exception;

    public static void main(String[] args) throws Throwable {
        // 模型建立成功，下面要进行使用
        SaveNodeInfo saveNodeInfo = parse2NFA("ab*c.*d.*d*");
        parse2NFA("a");
        parse2NFA(".*");
        System.out.println(isMatch("abcde","a*.*.a*a*b"));
        System.out.println(isMatch("a","a*.*.a*a*b*"));
        System.out.println(isMatch("mississippi","mis*is*ip*."));
        System.out.println(isMatch("aaaaaaaaaaaaab","a*a*a*a*a*a*a*a*a*a*b"));
        System.out.println(isMatch("bba","b*.*a*a*a"));
        System.out.println(isMatch("aasdfasdfasdfasdfas","aasdf.*asdf.*asdf.*asdf.*s"));
        System.out.println(isMatch("aasasasasas","aas.*as.*as.*.*s"));


    }

    @Test
    public void test1() throws Throwable {
        System.out.println(isMatch("ab", ".*ab.d*"));
        System.out.println(isMatch("abcd", ".*ab.dd*"));
    }

    static class Node {

        // 节点状态值
        char value;

        // 下一个状态
        Node nextNode;

        // 可直接转移状态集合
        List<Node> TransferStates = null;

        boolean isHead;

        public Node(char value) {
            this.value = value;
        }
    }

    /**
     * @param p 将p转换成NFA
     * @return
     */
    public static SaveNodeInfo parse2NFA(String p) throws Throwable {

        ArrayList<Character> arrayList = new ArrayList();

        // 记录a-z和.的正常个数，没有"*"后缀
        int recordNormal = 0;
        Node node = null;
        Node preNode = null;

        int length = p.length();
        // 如果p的长度为0或开头是*，直接抛出错误
        if (length == 0 || p.charAt(0) == '*') {
            throw (Throwable) Exception;
        }
        for (int i = 0; i < length; i++) {
            arrayList.add(p.charAt(i));
        }


        for (int i = 0; i < arrayList.size(); i++) {

            Node node1 = new Node(arrayList.get(i));
            if (i == 0) {
                // 记住头节点
                node = node1;
                node.isHead = true;
            }
            if (preNode != null) {
                preNode.nextNode = node1;
                if (preNode.TransferStates != null) {
                    preNode.TransferStates.add(node1);
                }
            }

            if ((i + 1) < arrayList.size() && arrayList.get(i + 1) == '*') {
                if (node1.TransferStates == null) {
                    node1.TransferStates = new ArrayList<>();
                }

                node1.TransferStates.add(node1);
                arrayList.remove(i + 1);
            } else {
                // 说明是正常的a-z或.
                recordNormal++;
            }
            preNode = node1;
        }

        return new SaveNodeInfo(recordNormal, node);
    }

    static class SaveNodeInfo {
        // 记录正常字符的个数
        int Normal;
        Node node;

        public SaveNodeInfo(int normal, Node node) {
            Normal = normal;
            this.node = node;
        }
    }


    public static boolean isMatch(String s, String p) throws Throwable {
        SaveNodeInfo saveNodeInfo = parse2NFA(p);
        if(s.length()==0 && saveNodeInfo.Normal==0){
            return true;
        }
        if(!(s.length()!=0 && p.length()!=0)){

            return false;
        }
        int sLen = s.length();
        // 如果s的字符长度小于p中必须存在的字符长度，则必然是false
        if (sLen < saveNodeInfo.Normal) {
            return false;
        }
        Node node = saveNodeInfo.node;

        // 如果s的字符长度等于p中必须存在的字符长度，则进行该字符的匹配
        // 只要存在一个不成功则失败
        if (sLen == saveNodeInfo.Normal) {

            for (int i = 0; i < sLen; i++) {
                while (node != null && node.TransferStates != null) {
                    node = node.nextNode;
                }
                if (s.charAt(i) != node.value && node.value != '.') {
                    return false;
                }
                node = node.nextNode;
            }
            return true;
        }

        // 下面这种为最复杂的情况，当s字符长度大于p的字符长度时，有多种可能，需逐一探索

        return isMatch(s, 0, node, sLen, saveNodeInfo.Normal);
    }

    /**
     * @param s          需要匹配的字符串
     * @param index      当前需匹配的位置
     * @param node       当前的节点
     * @param sLeft      s剩余需要匹配的字符串个数
     * @param recordLeft Node剩余存在的不可去除的符号
     * @return
     */
    public static boolean isMatch(String s, int index, Node node, int sLeft, int recordLeft) {

        // 当剩余的recordLeft大于sLeft则必然是false
        if (recordLeft > sLeft) {
            return false;
        }


        if (node != null && node.TransferStates != null) {

            // 情况一
            // 考虑该节点,匹配成功
            int sLeft1 = sLeft;
            int recordLeft1 = recordLeft;
            boolean isTry = false;
            if (s.charAt(index) == node.value || node.value == '.') {
                sLeft1--;
                if (sLeft1 == 0 && recordLeft1 == 0) {
                    return true;
                }else if(recordLeft1 > sLeft1){

                    // 如果不加这个，不考虑本节点的情况会被“截胡”，出现错误逻辑。
                    if (isMatch(s, index, node.nextNode, sLeft, recordLeft)) {
                        return true;
                    }

                    return false;
                }
                // 深度优先遍历
                for (Node node1 :
                        node.TransferStates) {
                    if (isMatch(s, index + 1, node1, sLeft1, recordLeft1)) {
                        return true;
                    }
                }



            } else {
                // 情况一
                // 考虑该节点，匹配不成功
                isTry = true;
                for (Node node1 : node.TransferStates) {
                    // 去除该节点
                    if (node1 != node) {
                        if (isMatch(s, index, node1, sLeft1, recordLeft1)) {
                            return true;
                        }
                    }
                }
            }

            // 即使该节点匹配成功
            // 情况二不考虑该节点
            if(!isTry){
                if (isMatch(s, index, node.nextNode, sLeft, recordLeft)) {
                    return true;
                }
            }



        } else if (node != null && node.TransferStates==null) {
            // 考虑是两个常规字符相匹配的情况
            if (!(s.charAt(index) == node.value || node.value == '.')) {
                return false;
            } else {
                sLeft--;
                recordLeft--;
                if (sLeft == 0 && recordLeft == 0) {
                    return true;
                }else if(recordLeft > sLeft){
                    return false;
                }
                if (isMatch(s, index+1, node.nextNode, sLeft, recordLeft)) {
                    return true;
                }
            }
        }

        return false;
    }


}


